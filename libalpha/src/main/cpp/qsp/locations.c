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

#include "locations.h"
#include "common.h"
#include "errors.h"
#include "game.h"
#include "statements.h"
#include "text.h"
#include "variables.h"

QSPLocation *qspLocs = 0;
QSPLocName *qspLocsNames = 0;
int qspLocsCount = 0;
int qspCurLoc = -1;
int qspLocationState = 0;
int qspFullRefreshCount = 0;

INLINE int qspLocsCompare(const void *locName1, const void *locName2);
INLINE int qspLocStringCompare(const void *name, const void *compareTo);
INLINE void qspExecLocByIndex(int locInd, QSP_BOOL toChangeDesc);

INLINE int qspLocsCompare(const void *locName1, const void *locName2)
{
    return qspStrsCompare(((QSPLocName *)locName1)->Name, ((QSPLocName *)locName2)->Name);
}

INLINE int qspLocStringCompare(const void *name, const void *compareTo)
{
    return qspStrsCompare(*(QSPString *)name, ((QSPLocName *)compareTo)->Name);
}

void qspCreateWorld(int start, int newLocsCount)
{
    int i, j;
    /* Release overlapping locations */
    for (i = start; i < qspLocsCount; ++i)
    {
        qspFreeString(&qspLocsNames[i].Name);
        qspFreeString(&qspLocs[i].Name);
        qspFreeString(&qspLocs[i].Desc);
        qspFreePrepLines(qspLocs[i].OnVisitLines, qspLocs[i].OnVisitLinesCount);
        for (j = 0; j < qspLocs[i].ActionsCount; ++j)
        {
            qspFreeString(&qspLocs[i].Actions[j].Image);
            qspFreeString(&qspLocs[i].Actions[j].Desc);
            qspFreePrepLines(qspLocs[i].Actions[j].OnPressLines, qspLocs[i].Actions[j].OnPressLinesCount);
        }
    }
    /* Reallocate resources, we can either increase or decrease the number of locations here */
    if (qspLocsCount != newLocsCount)
    {
        qspLocsCount = newLocsCount;
        qspLocs = (QSPLocation *)realloc(qspLocs, qspLocsCount * sizeof(QSPLocation));
        qspLocsNames = (QSPLocName *)realloc(qspLocsNames, qspLocsCount * sizeof(QSPLocName));
    }
    /* Init locations */
    for (i = start; i < qspLocsCount; ++i)
    {
        qspLocsNames[i].Name = qspNullString;
        qspLocs[i].Name = qspNullString;
        qspLocs[i].Desc = qspNullString;
        qspLocs[i].OnVisitLines = 0;
        qspLocs[i].OnVisitLinesCount = 0;
        qspLocs[i].ActionsCount = 0;
    }
}

void qspPrepareLocs(void)
{
    int i;
    for (i = 0; i < qspLocsCount; ++i)
    {
        qspLocsNames[i].Index = i;
        qspUpdateText(&qspLocsNames[i].Name, qspLocs[i].Name);
        qspUpperStr(&qspLocsNames[i].Name);
    }
    qsort(qspLocsNames, qspLocsCount, sizeof(QSPLocName), qspLocsCompare);
}

int qspLocIndex(QSPString name)
{
    QSPLocName *loc;
    if (!qspLocsCount) return -1;
    name = qspDelSpc(name);
    if (qspIsEmpty(name)) return -1;
    name = qspCopyToNewText(name);
    qspUpperStr(&name);
    loc = (QSPLocName *)bsearch(&name, qspLocsNames, qspLocsCount, sizeof(QSPLocName), qspLocStringCompare);
    qspFreeString(&name);
    if (loc) return loc->Index;
    return -1;
}

INLINE void qspExecLocByIndex(int locInd, QSP_BOOL toChangeDesc)
{
    QSPString str, actionName;
    QSPLineOfCode *oldLine;
    int i, oldLoc, oldActIndex, oldLineNum, oldLocationState = qspLocationState;
    QSPLocation *loc = qspLocs + locInd;
    /* Remember the previous state to restore it after internal calls */
    oldLoc = qspRealCurLoc;
    oldActIndex = qspRealActIndex;
    oldLineNum = qspRealLineNum;
    oldLine = qspRealLine;
    /* Switch the current state */
    qspRealCurLoc = locInd;
    qspRealActIndex = -1;
    qspRealLineNum = 0;
    qspRealLine = 0;
    /* Update base description */
    str = qspFormatText(loc->Desc, QSP_FALSE);
    if (qspLocationState != oldLocationState)
    {
        qspRealLine = oldLine;
        qspRealLineNum = oldLineNum;
        qspRealActIndex = oldActIndex;
        qspRealCurLoc = oldLoc;
        return;
    }
    if (toChangeDesc)
    {
        qspFreeBufString(&qspCurDesc);
        qspCurDesc = qspStringToBufString(str, 512);
        qspIsMainDescChanged = QSP_TRUE;
    }
    else
    {
        if (qspAddBufText(&qspCurDesc, str))
            qspIsMainDescChanged = QSP_TRUE;
        qspFreeString(&str);
    }
    /* Update base actions */
    for (i = 0; i < loc->ActionsCount; ++i)
    {
        actionName = loc->Actions[i].Desc;
        if (qspIsEmpty(actionName)) break;
        actionName = qspFormatText(actionName, QSP_FALSE);
        if (qspLocationState != oldLocationState)
        {
            qspRealLine = oldLine;
            qspRealLineNum = oldLineNum;
            qspRealActIndex = oldActIndex;
            qspRealCurLoc = oldLoc;
            return;
        }
        qspRealActIndex = i;
        str = loc->Actions[i].Image;
        if (!qspIsEmpty(str))
            qspAddAction(actionName, str, loc->Actions[i].OnPressLines, 0, loc->Actions[i].OnPressLinesCount);
        else
            qspAddAction(actionName, qspNullString, loc->Actions[i].OnPressLines, 0, loc->Actions[i].OnPressLinesCount);
        qspFreeString(&actionName);
        if (qspErrorNum)
        {
            qspRealLine = oldLine;
            qspRealLineNum = oldLineNum;
            qspRealActIndex = oldActIndex;
            qspRealCurLoc = oldLoc;
            return;
        }
    }
    /* Execute the code */
    qspRealActIndex = -1;
    if (locInd < qspLocsCount - qspCurIncLocsCount) /* location is inside the base game file */
        qspExecCode(loc->OnVisitLines, 0, loc->OnVisitLinesCount, 1, 0);
    else
    {
        QSPLineOfCode *code;
        int count = loc->OnVisitLinesCount;
        qspCopyPrepLines(&code, loc->OnVisitLines, 0, count);
        qspExecCode(code, 0, count, 1, 0);
        qspFreePrepLines(code, count);
    }
    /* Restore the old state */
    qspRealLine = oldLine;
    qspRealLineNum = oldLineNum;
    qspRealActIndex = oldActIndex;
    qspRealCurLoc = oldLoc;
}

void qspExecLocByNameWithArgs(QSPString name, QSPVariant *args, QSP_TINYINT argsCount, QSP_BOOL toMoveArgs, QSPVariant *res)
{
    int oldLocationState, locInd = qspLocIndex(name);
    if (locInd < 0)
    {
        qspSetError(QSP_ERR_LOCNOTFOUND);
        return;
    }
    qspAllocateSavedVarsGroupWithArgs();
    qspSetArgs(args, argsCount, toMoveArgs);
    oldLocationState = qspLocationState;
    qspExecLocByIndex(locInd, QSP_FALSE);
    if (qspLocationState != oldLocationState)
    {
        qspClearLastSavedVarsGroup();
        return;
    }
    if (res) qspApplyResult(res);
    qspRestoreLastSavedVarsGroup();
}

void qspExecLocByVarNameWithArgs(QSPString name, QSPVariant *args, QSP_TINYINT argsCount)
{
    QSPVar *var;
    QSPString locName;
    QSPVarsGroup *savedVarGroups;
    int ind, oldLocationState, savedVarGroupsCount;
    /* Restore global variables */
    savedVarGroupsCount = qspSaveLocalVarsAndRestoreGlobals(&savedVarGroups);
    if (qspErrorNum) return;
    /* We execute all locations specified in the array */
    oldLocationState = qspLocationState;
    ind = 0;
    while (1)
    {
        /* The variable might be updated during the previous code execution */
        if (!((var = qspVarReference(name, QSP_FALSE))))
        {
            qspClearSavedLocalVars(savedVarGroups, savedVarGroupsCount);
            return;
        }
        if (ind >= var->ValsCount) break;
        if (!QSP_ISSTR(var->Values[ind].Type)) break;
        locName = QSP_STR(var->Values[ind]);
        if (!qspIsAnyString(locName)) break;
        qspExecLocByNameWithArgs(locName, args, argsCount, QSP_FALSE, 0);
        if (qspLocationState != oldLocationState)
        {
            qspClearSavedLocalVars(savedVarGroups, savedVarGroupsCount);
            return;
        }
        ++ind;
    }
    qspRestoreSavedLocalVars(savedVarGroups, savedVarGroupsCount);
}

void qspNavigateToLocation(int locInd, QSP_BOOL toChangeDesc, QSPVariant *args, QSP_TINYINT argsCount)
{
    int oldLocationState;
    if (locInd < 0 || locInd >= qspLocsCount) return;
    qspCurLoc = locInd;
    qspRestoreGlobalVars(); /* clean all local variables */
    if (qspErrorNum) return;
    /* We assign global ARGS here */
    qspSetArgs(args, argsCount, QSP_FALSE);
    qspClearAllActions(QSP_FALSE);
    ++qspLocationState;
    if (toChangeDesc) ++qspFullRefreshCount;
    qspAllocateSavedVarsGroup();
    oldLocationState = qspLocationState;
    qspExecLocByIndex(locInd, toChangeDesc);
    if (qspLocationState != oldLocationState)
    {
        qspClearLastSavedVarsGroup();
        return;
    }
    qspRestoreLastSavedVarsGroup();
    if (qspErrorNum) return;
    qspExecLocByVarNameWithArgs(QSP_STATIC_STR(QSP_LOC_NEWLOC), args, argsCount);
}
