/* Copyright (C) 2001-2024 Val Argunov (byte AT qsp DOT org) */
/*
* This library is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2.1 of the License, or
* (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/

#include "codetools.h"
#include "statements.h"
#include "text.h"

INLINE int qspStatStringCompare(const void *name, const void *compareTo);
INLINE QSP_TINYINT qspGetStatCode(QSPString s, QSP_CHAR **pos);
INLINE QSP_TINYINT qspInitStatArgs(QSPCachedArg **args, QSP_TINYINT statCode, QSPString s, QSP_CHAR *origStart, QSP_TINYINT *errorCode);
INLINE QSP_TINYINT qspInitSetArgs(QSPCachedArg **args, QSP_TINYINT statCode, QSPString s, QSP_CHAR *origStart, QSP_TINYINT *errorCode);
INLINE QSP_TINYINT qspInitSingleArg(QSPCachedArg **args, QSP_TINYINT statCode, QSPString s, QSP_CHAR *origStart, QSP_TINYINT *errorCode);
INLINE QSP_TINYINT qspInitRegularArgs(QSPCachedArg **args, QSP_TINYINT statCode, QSPString s, QSP_CHAR *origStart, QSP_TINYINT *errorCode);
INLINE QSP_BOOL qspAppendLineToResult(QSPString str, int lineNum, QSPBufString *strBuf, QSPLineOfCode *line);
INLINE void qspAppendLastLineToResult(QSPString str, int lineNum, QSPBufString *strBuf, QSPLineOfCode *line);

INLINE int qspStatStringCompare(const void *name, const void *compareTo)
{
    QSPStatName *statName = (QSPStatName *)compareTo;
    return qspStrsPartCompare(*(QSPString *)name, statName->Name, qspStrLen(statName->Name));
}

INLINE QSP_TINYINT qspGetStatCode(QSPString s, QSP_CHAR **pos)
{
    int i, strLen, nameLen;
    QSPStatName *name;
    if (qspIsEmpty(s)) return qspStatUnknown;
    if (*s.Str == QSP_LABEL[0]) return qspStatLabel;
    if (*s.Str == QSP_COMMENT[0]) return qspStatComment;
    /* ------------------------------------------------------------------ */
    strLen = qspStrLen(s);
    for (i = 0; i < QSP_STATSLEVELS; ++i)
    {
        name = (QSPStatName *)bsearch(&s, qspStatsNames[i], qspStatsNamesCounts[i], sizeof(QSPStatName), qspStatStringCompare);
        if (name)
        {
            nameLen = qspStrLen(name->Name);
            if (nameLen == strLen || (nameLen < strLen && qspIsInClass(s.Str[nameLen], QSP_CHAR_DELIM)))
            {
                *pos = s.Str + nameLen;
                return name->Code;
            }
        }
    }
    return qspStatUnknown;
}

INLINE QSP_TINYINT qspInitStatArgs(QSPCachedArg **args, QSP_TINYINT statCode, QSPString s, QSP_CHAR *origStart, QSP_TINYINT *errorCode)
{
    *args = 0;
    *errorCode = 0;
    switch (statCode)
    {
        case qspStatUnknown:
        case qspStatLabel:
        case qspStatElse:
        case qspStatEnd:
        case qspStatComment:
        case qspStatLoop:
            return 0;
        case qspStatSet:
        case qspStatLocal:
            return qspInitSetArgs(args, statCode, s, origStart, errorCode);
        case qspStatImplicitStatement:
        case qspStatIf:
        case qspStatElseIf:
            return qspInitSingleArg(args, statCode, s, origStart, errorCode);
        default:
            return qspInitRegularArgs(args, statCode, s, origStart, errorCode);
    }
}

INLINE QSP_TINYINT qspInitSetArgs(QSPCachedArg **args, QSP_TINYINT QSP_UNUSED(statCode), QSPString s, QSP_CHAR *origStart, QSP_TINYINT *errorCode)
{
    QSP_TINYINT argsCount;
    QSPCachedArg *foundArgs;
    QSP_CHAR *pos;
    qspSkipSpaces(&s);
    pos = qspDelimPos(s, QSP_EQUAL[0]);
    if (pos)
    {
        QSPString names, values, op;
        op = qspStringFromPair(pos, pos + QSP_STATIC_LEN(QSP_EQUAL));
        values = qspDelSpc(qspStringFromPair(op.End, s.End));
        if (op.Str != s.Str && qspIsInClass(*(op.Str - 1), QSP_CHAR_SIMPLEOP)) --op.Str;
        names = qspDelSpc(qspStringFromPair(s.Str, op.Str));
        if (qspIsEmpty(names) || qspIsEmpty(values))
            *errorCode = QSP_ERR_SYNTAX;

        argsCount = 3;
        foundArgs = (QSPCachedArg *)malloc(argsCount * sizeof(QSPCachedArg));
        foundArgs[0].StartPos = (int)(names.Str - origStart);
        foundArgs[0].EndPos = (int)(names.End - origStart);
        foundArgs[1].StartPos = (int)(op.Str - origStart);
        foundArgs[1].EndPos = (int)(op.End - origStart);
        foundArgs[2].StartPos = (int)(values.Str - origStart);
        foundArgs[2].EndPos = (int)(values.End - origStart);
    }
    else
    {
        QSPString names = qspDelSpc(s);
        if (qspIsEmpty(names))
            *errorCode = QSP_ERR_SYNTAX;

        argsCount = 1;
        foundArgs = (QSPCachedArg *)malloc(argsCount * sizeof(QSPCachedArg));
        foundArgs[0].StartPos = (int)(names.Str - origStart);
        foundArgs[0].EndPos = (int)(names.End - origStart);
    }
    *args = foundArgs;
    return argsCount;
}

INLINE QSP_TINYINT qspInitSingleArg(QSPCachedArg **args, QSP_TINYINT statCode, QSPString s, QSP_CHAR *origStart, QSP_TINYINT *errorCode)
{
    QSPCachedArg *foundArgs = 0;
    QSP_TINYINT argsCount = 0;
    qspSkipSpaces(&s);
    if (!qspIsEmpty(s))
    {
        /* Consider the whole string as 1 argument */
        if (qspStats[statCode].MaxArgsCount)
        {
            argsCount = 1;
            foundArgs = (QSPCachedArg *)malloc(sizeof(QSPCachedArg));
            foundArgs[0].StartPos = (int)(s.Str - origStart);
            foundArgs[0].EndPos = (int)(s.End - origStart);
        }
        else
            *errorCode = QSP_ERR_ARGSCOUNT;
    }
    if (argsCount < qspStats[statCode].MinArgsCount)
        *errorCode = QSP_ERR_ARGSCOUNT;
    *args = foundArgs;
    return argsCount;
}

INLINE QSP_TINYINT qspInitRegularArgs(QSPCachedArg **args, QSP_TINYINT statCode, QSPString s, QSP_CHAR *origStart, QSP_TINYINT *errorCode)
{
    QSPCachedArg *foundArgs = 0;
    QSP_TINYINT argsCount = 0;
    qspSkipSpaces(&s);
    if (!qspIsEmpty(s))
    {
        QSP_CHAR *pos;
        int bufSize = 0;
        if (*s.Str == QSP_LRBRACK[0]) /* arguments might be specified using parentheses */
        {
            QSP_CHAR *bracket;
            if (!(bracket = qspDelimPos(s, QSP_RRBRACK[0])))
            {
                *errorCode = QSP_ERR_BRACKNOTFOUND;
                return 0;
            }
            if (!qspIsAnyString(qspStringFromPair(bracket + QSP_STATIC_LEN(QSP_RRBRACK), s.End)))
            {
                /* We'll parse arguments between parentheses */
                s = qspStringFromPair(s.Str + QSP_STATIC_LEN(QSP_LRBRACK), bracket);
                qspSkipSpaces(&s);
            }
        }
        while (1)
        {
            if (argsCount >= qspStats[statCode].MaxArgsCount)
            {
                *errorCode = QSP_ERR_ARGSCOUNT;
                break;
            }
            if (argsCount >= bufSize)
            {
                bufSize = argsCount + 4;
                foundArgs = (QSPCachedArg *)realloc(foundArgs, bufSize * sizeof(QSPCachedArg));
            }
            pos = qspDelimPos(s, QSP_COMMA[0]);
            if (pos)
            {
                foundArgs[argsCount].StartPos = (int)(s.Str - origStart);
                foundArgs[argsCount].EndPos = (int)(pos - origStart);
                ++argsCount;
            }
            else
            {
                foundArgs[argsCount].StartPos = (int)(s.Str - origStart);
                foundArgs[argsCount].EndPos = (int)(s.End - origStart);
                ++argsCount;
                break;
            }
            s.Str = pos + QSP_STATIC_LEN(QSP_COMMA);
            qspSkipSpaces(&s);
            if (qspIsEmpty(s))
            {
                *errorCode = QSP_ERR_SYNTAX;
                break;
            }
        }
    }
    if (argsCount < qspStats[statCode].MinArgsCount)
        *errorCode = QSP_ERR_ARGSCOUNT;
    *args = foundArgs;
    return argsCount;
}

QSPString qspGetLineLabel(QSPString str)
{
    qspSkipSpaces(&str);
    if (!qspIsEmpty(str) && *str.Str == QSP_LABEL[0])
    {
        QSP_CHAR *delimPos = qspStrChar(str, QSP_STATDELIM[0]);
        if (delimPos)
            str = qspStringFromPair(str.Str + QSP_STATIC_LEN(QSP_LABEL), delimPos);
        else
            str = qspStringFromPair(str.Str + QSP_STATIC_LEN(QSP_LABEL), str.End);
        str = qspCopyToNewText(qspDelSpc(str));
        qspUpperStr(&str);
        return str;
    }
    return qspNullString;
}

void qspInitLineOfCode(QSPLineOfCode *line, QSPString str, int lineNum)
{
    int statInd;
    QSP_TINYINT statCode;
    QSP_CHAR *statDelimPos, *paramPos;
    /* 'nextPos' points to the next position to search for a statement */
    /* 'statDelimPos' points to the statement separator (':' or '&') */
    line->Str = str;
    line->Label = qspNullString;
    line->LineNum = lineNum;
    line->LinesToElse = line->LinesToEnd = 0;
    line->IsMultiline = QSP_FALSE;
    line->StatsCount = 0;
    line->Stats = 0;
    qspSkipSpaces(&str);
    if (qspIsEmpty(str)) return;
    statInd = 0;
    statDelimPos = paramPos = 0;
    statCode = qspGetStatCode(str, &paramPos);
    if (statCode != qspStatComment)
    {
        QSP_CHAR *temp, *elsePos = 0, *nextPos = 0;
        QSP_BOOL toSearchElse = QSP_TRUE;
        switch (statCode)
        {
        case qspStatAct:
        case qspStatLoop:
        case qspStatIf:
        case qspStatElseIf:
            statDelimPos = qspDelimPos(str, QSP_COLONDELIM[0]);
            if (statDelimPos)
            {
                nextPos = statDelimPos + QSP_STATIC_LEN(QSP_COLONDELIM);
                if (nextPos == str.End) nextPos = 0;
            }
            break;
        case qspStatElse:
            str.Str = paramPos;
            qspSkipSpaces(&str);
            nextPos = str.Str;
            if (nextPos < str.End)
            {
                statDelimPos = nextPos;
                if (*nextPos == QSP_COLONDELIM[0])
                {
                    nextPos += QSP_STATIC_LEN(QSP_COLONDELIM);
                    if (nextPos == str.End) nextPos = 0;
                }
            }
            break;
        default:
            statDelimPos = qspDelimPos(str, QSP_STATDELIM[0]);
            if (statDelimPos) nextPos = statDelimPos + QSP_STATIC_LEN(QSP_STATDELIM);
            elsePos = qspStrPos(str, QSP_STATIC_STR(QSP_STATELSE), QSP_TRUE);
            temp = qspStrPos(str, QSP_STATIC_STR(QSP_STATELSEIF), QSP_TRUE);
            if (temp && !(elsePos && elsePos < temp)) elsePos = temp; /* keep ELSE if it goes before ELSEIF */
            if (elsePos)
            {
                if (!statDelimPos || elsePos < statDelimPos)
                {
                    nextPos = statDelimPos = elsePos;
                    elsePos = 0;
                }
            }
            else
                toSearchElse = QSP_FALSE;
            if (statCode == qspStatUnknown && str.Str != statDelimPos)
            {
                if (statDelimPos)
                    temp = qspDelimPos(qspStringFromPair(str.Str, statDelimPos), QSP_EQUAL[0]);
                else
                    temp = qspDelimPos(str, QSP_EQUAL[0]);
                statCode = (temp ? qspStatSet : qspStatImplicitStatement);
            }
            break;
        }
        while (statDelimPos && nextPos)
        {
            line->StatsCount++;
            line->Stats = (QSPCachedStat *)realloc(line->Stats, line->StatsCount * sizeof(QSPCachedStat));
            line->Stats[statInd].Stat = statCode;
            if (paramPos)
            {
                str.Str = paramPos;
                qspSkipSpaces(&str);
            }
            line->Stats[statInd].ParamPos = (int)(str.Str - line->Str.Str);
            line->Stats[statInd].EndPos = (int)(statDelimPos - line->Str.Str);
            line->Stats[statInd].ArgsCount = qspInitStatArgs(&line->Stats[statInd].Args, statCode, qspStringFromPair(str.Str, statDelimPos), line->Str.Str, &line->Stats[statInd].ErrorCode);
            ++statInd;
            str.Str = nextPos;
            qspSkipSpaces(&str);
            paramPos = 0;
            statCode = qspGetStatCode(str, &paramPos);
            if (!qspIsEmpty(str) && statCode != qspStatComment)
            {
                switch (statCode)
                {
                case qspStatAct:
                case qspStatLoop:
                case qspStatIf:
                case qspStatElseIf:
                    statDelimPos = qspDelimPos(str, QSP_COLONDELIM[0]);
                    if (statDelimPos)
                    {
                        nextPos = statDelimPos + QSP_STATIC_LEN(QSP_COLONDELIM);
                        if (nextPos == str.End) nextPos = 0;
                    }
                    break;
                case qspStatElse:
                    str.Str = paramPos;
                    qspSkipSpaces(&str);
                    nextPos = str.Str;
                    if (nextPos < str.End)
                    {
                        statDelimPos = nextPos;
                        if (*nextPos == QSP_COLONDELIM[0])
                        {
                            nextPos += QSP_STATIC_LEN(QSP_COLONDELIM);
                            if (nextPos == str.End) nextPos = 0;
                        }
                    }
                    else
                        statDelimPos = 0;
                    break;
                default:
                    statDelimPos = qspDelimPos(str, QSP_STATDELIM[0]);
                    if (statDelimPos) nextPos = statDelimPos + QSP_STATIC_LEN(QSP_STATDELIM);
                    if (elsePos && str.Str >= elsePos) elsePos = 0;
                    if (!elsePos && toSearchElse)
                    {
                        elsePos = qspStrPos(str, QSP_STATIC_STR(QSP_STATELSE), QSP_TRUE);
                        temp = qspStrPos(str, QSP_STATIC_STR(QSP_STATELSEIF), QSP_TRUE);
                        if (temp && !(elsePos && elsePos < temp)) elsePos = temp; /* keep ELSE if it goes before ELSEIF */
                        if (!elsePos) toSearchElse = QSP_FALSE;
                    }
                    if (elsePos && (!statDelimPos || elsePos < statDelimPos))
                    {
                        nextPos = statDelimPos = elsePos;
                        elsePos = 0;
                    }
                    if (statCode == qspStatUnknown && str.Str != statDelimPos)
                    {
                        if (statDelimPos)
                            temp = qspDelimPos(qspStringFromPair(str.Str, statDelimPos), QSP_EQUAL[0]);
                        else
                            temp = qspDelimPos(str, QSP_EQUAL[0]);
                        statCode = (temp ? qspStatSet : qspStatImplicitStatement);
                    }
                    break;
                }
            }
            else
                statDelimPos = 0;
        }
    }
    /* Check for ELSE IF */
    if (statInd == 1
        && line->Stats[0].Stat == qspStatElse && statCode == qspStatIf
        && !qspIsCharAtPos(line->Str, line->Str.Str + line->Stats[0].ParamPos, QSP_COLONDELIM[0]))
    {
        /* Convert multiline ELSE IF to ELSEIF */
        statCode = qspStatElseIf; /* move current IF as ELSEIF to index 0, it's safe to overwrite ELSE */
        statInd = 0;
    }
    else if (statInd == 2
        && line->Stats[0].Stat == qspStatElse && line->Stats[1].Stat == qspStatIf && statCode == qspStatComment
        && !qspIsCharAtPos(line->Str, line->Str.Str + line->Stats[0].ParamPos, QSP_COLONDELIM[0]))
    {
        /* Convert multiline ELSE IF with a comment to ELSEIF with the comment */
        line->Stats[0].Stat = qspStatElseIf; /* move IF as ELSEIF to index 0, it's safe to overwrite ELSE */
        line->Stats[0].ParamPos = line->Stats[1].ParamPos;
        line->Stats[0].EndPos = line->Stats[1].EndPos;
        line->Stats[0].ArgsCount = line->Stats[1].ArgsCount;
        line->Stats[0].Args = line->Stats[1].Args;
        line->Stats[0].ErrorCode = line->Stats[1].ErrorCode;
        statInd = 1; /* move current comment to index 1 */
    }
    else
    {
        line->StatsCount++;
        line->Stats = (QSPCachedStat *)realloc(line->Stats, line->StatsCount * sizeof(QSPCachedStat));
    }
    /* Add the last statement */
    line->Stats[statInd].Stat = statCode;
    if (paramPos)
    {
        str.Str = paramPos;
        qspSkipSpaces(&str);
    }
    line->Stats[statInd].ParamPos = (int)(str.Str - line->Str.Str);
    if (statDelimPos)
    {
        line->Stats[statInd].EndPos = (int)(statDelimPos - line->Str.Str);
        line->Stats[statInd].ArgsCount = qspInitStatArgs(&line->Stats[statInd].Args, statCode, qspStringFromPair(str.Str, statDelimPos), line->Str.Str, &line->Stats[statInd].ErrorCode);
    }
    else
    {
        line->Stats[statInd].EndPos = (int)(str.End - line->Str.Str);
        line->Stats[statInd].ArgsCount = qspInitStatArgs(&line->Stats[statInd].Args, statCode, str, line->Str.Str, &line->Stats[statInd].ErrorCode);
    }
    switch (line->Stats[0].Stat)
    {
    case qspStatAct:
    case qspStatLoop:
    case qspStatIf:
    case qspStatElseIf:
        if (qspIsCharAtPos(line->Str, line->Str.Str + line->Stats[0].EndPos, QSP_COLONDELIM[0]))
        {
            if (line->StatsCount == 1)
                line->IsMultiline = QSP_TRUE;
            else if (line->StatsCount == 2 && line->Stats[1].Stat == qspStatComment)
                line->IsMultiline = QSP_TRUE;
        }
        /* Always search next ELSE/END starting next line since we don't have all the lines ready to find the right ones yet */
        line->LinesToEnd = line->LinesToElse = 1;
        break;
    case qspStatElse:
        if (line->StatsCount == 1)
            line->IsMultiline = QSP_TRUE;
        else if (line->StatsCount == 2 && line->Stats[1].Stat == qspStatComment)
            line->IsMultiline = QSP_TRUE;
        /* Always search next ELSE/END starting next line since we don't have all the lines ready to find the right ones yet */
        line->LinesToEnd = line->LinesToElse = 1;
        break;
    }
    line->Label = qspGetLineLabel(line->Str);
}

void qspFreeLineOfCode(QSPLineOfCode *line)
{
    /* We don't release the line text here */
    qspFreeString(&line->Label);
    if (line->Stats)
    {
        int i;
        QSPCachedStat *stat = line->Stats;
        for (i = 0; i < line->StatsCount; ++i, ++stat)
            if (stat->Args) free(stat->Args);
        free(line->Stats);
    }
}

void qspFreePrepLines(QSPLineOfCode *strs, int count)
{
    if (strs)
    {
        QSPLineOfCode *curStr = strs;
        while (--count >= 0)
        {
            qspFreeString(&curStr->Str);
            qspFreeLineOfCode(curStr);
            ++curStr;
        }
        free(strs);
    }
}

void qspCopyPrepStatements(QSPCachedStat **dest, QSPCachedStat *src, int start, int end, int codeOffset)
{
    int statsCount = end - start;
    if (src && statsCount)
    {
        QSP_TINYINT i, argsCount;
        QSPCachedStat *stat;
        *dest = (QSPCachedStat *)malloc(statsCount * sizeof(QSPCachedStat));
        stat = *dest;
        while (start < end)
        {
            stat->Stat = src[start].Stat;
            stat->ParamPos = src[start].ParamPos - codeOffset;
            stat->EndPos = src[start].EndPos - codeOffset;
            stat->ErrorCode = src[start].ErrorCode;
            argsCount = stat->ArgsCount = src[start].ArgsCount;
            if (argsCount)
            {
                stat->Args = (QSPCachedArg *)malloc(argsCount * sizeof(QSPCachedArg));
                for (i = 0; i < argsCount; ++i)
                {
                    stat->Args[i].StartPos = src[start].Args[i].StartPos - codeOffset;
                    stat->Args[i].EndPos = src[start].Args[i].EndPos - codeOffset;
                }
            }
            else
                stat->Args = 0;
            ++stat;
            ++start;
        }
    }
    else
        *dest = 0;
}

void qspCopyPrepLines(QSPLineOfCode **dest, QSPLineOfCode *src, int start, int end)
{
    int linesCount = end - start;
    if (src && linesCount)
    {
        QSPLineOfCode *line;
        *dest = (QSPLineOfCode *)malloc(linesCount * sizeof(QSPLineOfCode));
        line = *dest;
        while (start < end)
        {
            line->Str = qspCopyToNewText(src[start].Str);
            line->LineNum = src[start].LineNum;
            line->LinesToEnd = src[start].LinesToEnd;
            line->LinesToElse = src[start].LinesToElse;
            line->IsMultiline = src[start].IsMultiline;
            line->Label = qspCopyToNewText(src[start].Label);
            line->StatsCount = src[start].StatsCount;
            qspCopyPrepStatements(&line->Stats, src[start].Stats, 0, src[start].StatsCount, 0);
            ++line;
            ++start;
        }
    }
    else
        *dest = 0;
}

QSPString qspJoinPrepLines(QSPLineOfCode *s, int count, QSPString delim)
{
    int i;
    QSPBufString res = qspNewBufString(256);
    for (i = 0; i < count; ++i)
    {
        qspAddBufText(&res, s[i].Str);
        if (i == count - 1) break; /* don't add the delim */
        qspAddBufText(&res, delim);
    }
    return qspBufTextToString(res);
}

void qspPrepareStringToExecution(QSPString *str)
{
    int quotsCount = 0;
    QSP_CHAR *pos = str->Str;
    while (pos < str->End)
    {
        if (*pos == QSP_LQUOT[0])
            ++quotsCount;
        else if (*pos == QSP_RQUOT[0])
        {
            if (quotsCount) --quotsCount;
        }
        else if (qspIsInClass(*pos, QSP_CHAR_QUOT))
        {
            QSP_CHAR quot = *pos;
            while (++pos < str->End)
            {
                if (*pos == quot)
                {
                    ++pos;
                    if (pos >= str->End) break;
                    if (*pos != quot) break;
                }
            }
            continue;
        }
        else if (!quotsCount) /* we have to keep q-strings untouched */
            *pos = QSP_CHRUPR(*pos);
        ++pos;
    }
}

INLINE QSP_BOOL qspAppendLineToResult(QSPString str, int lineNum, QSPBufString *strBuf, QSPLineOfCode *line)
{
    QSPString lineStr;
    int eolLen = QSP_STATIC_LEN(QSP_PREEOLEXT QSP_EOLEXT);
    /* Check line ending only if we add something to the combined line */
    if (qspAddBufText(strBuf, str) && strBuf->Len >= eolLen)
    {
        QSPString eol = qspStringFromLen(strBuf->Str + strBuf->Len - eolLen, eolLen);
        if (!qspStrsCompare(eol, QSP_STATIC_STR(QSP_PREEOLEXT QSP_EOLEXT)))
        {
            strBuf->Len -= QSP_STATIC_LEN(QSP_EOLEXT); /* keep QSP_PREEOLEXT */
            return QSP_FALSE;
        }
    }
    lineStr = qspBufTextToString(*strBuf);
    /* Prepare the buffer to execution */
    qspPrepareStringToExecution(&lineStr);
    /* Transfer ownership of the buffer to QSPLineOfCode */
    qspInitLineOfCode(line, lineStr, lineNum);
    return QSP_TRUE;
}

INLINE void qspAppendLastLineToResult(QSPString str, int lineNum, QSPBufString *strBuf, QSPLineOfCode *line)
{
    QSPString lineStr;
    qspAddBufText(strBuf, str);
    lineStr = qspBufTextToString(*strBuf);
    /* Prepare the buffer to execution */
    qspPrepareStringToExecution(&lineStr);
    /* Transfer ownership of the buffer to QSPLineOfCode */
    qspInitLineOfCode(line, lineStr, lineNum);
}

int qspPreprocessData(QSPString data, QSPLineOfCode **strs)
{
    QSPLineOfCode *lines;
    QSPBufString combinedStrBuf;
    QSP_BOOL isNewLine;
    QSP_CHAR *str, *pos, quot = 0;
    int quotsCount = 0, lineNum = 0, lastLineNum = 0, linesCount = 0, linesBufSize = 8, strLen = 0, strBufSize = 256;
    if (qspIsEmpty(data))
    {
        *strs = 0;
        return 0;
    }
    str = (QSP_CHAR *)malloc(strBufSize * sizeof(QSP_CHAR));
    lines = (QSPLineOfCode *)malloc(linesBufSize * sizeof(QSPLineOfCode));
    combinedStrBuf = qspNewBufString(64);
    pos = data.Str;
    while (pos < data.End)
    {
        isNewLine = (qspStrsPartCompare(data, QSP_STATIC_STR(QSP_STRSDELIM), QSP_STATIC_LEN(QSP_STRSDELIM)) == 0);
        if (isNewLine) ++lineNum;
        if (quotsCount || quot || !isNewLine)
        {
            /* Keep composing the current line */
            if (strLen >= strBufSize)
            {
                strBufSize = strLen + 256;
                str = (QSP_CHAR *)realloc(str, strBufSize * sizeof(QSP_CHAR));
            }
            str[strLen++] = *pos;
            if (quot)
            {
                if (*pos++ == quot)
                {
                    if (pos < data.End && *pos == quot)
                    {
                        /* Escape code */
                        if (strLen >= strBufSize)
                        {
                            strBufSize = strLen + 256;
                            str = (QSP_CHAR *)realloc(str, strBufSize * sizeof(QSP_CHAR));
                        }
                        str[strLen++] = *pos++;
                    }
                    else
                    {
                        /* Termination of the string */
                        quot = 0;
                    }
                }
            }
            else
            {
                if (*pos == QSP_LQUOT[0])
                    ++quotsCount;
                else if (*pos == QSP_RQUOT[0])
                {
                    if (quotsCount) --quotsCount;
                }
                else if (qspIsInClass(*pos, QSP_CHAR_QUOT))
                    quot = *pos;
                ++pos;
            }
        }
        else
        {
            /* New line has been found */
            if (linesCount >= linesBufSize)
            {
                linesBufSize = linesCount + 16;
                lines = (QSPLineOfCode *)realloc(lines, linesBufSize * sizeof(QSPLineOfCode));
            }
            if (qspAppendLineToResult(qspDelSpc(qspStringFromLen(str, strLen)), lastLineNum, &combinedStrBuf, lines + linesCount))
            {
                combinedStrBuf = qspNewBufString(64);
                lastLineNum = lineNum;
                ++linesCount;
            }
            strLen = 0;
            pos += QSP_STATIC_LEN(QSP_STRSDELIM);
        }
        data.Str = pos;
    }
    if (linesCount >= linesBufSize)
        lines = (QSPLineOfCode *)realloc(lines, (linesCount + 1) * sizeof(QSPLineOfCode));
    qspAppendLastLineToResult(qspDelSpc(qspStringFromLen(str, strLen)), lastLineNum, &combinedStrBuf, lines + linesCount);
    ++linesCount;
    free(str);
    *strs = lines;
    return linesCount;
}
