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

#include "variant.h"
#include "errors.h"
#include "text.h"
#include "tuples.h"

static const QSP_BOOL qspTypeConversionTable[QSP_TYPE_DEFINED_TYPES][QSP_TYPE_DEFINED_TYPES] =
{
    /*             TUPLE     NUMBER    STRING     CODE      VARREF */
    /* TUPLE */  { QSP_TRUE, QSP_TRUE, QSP_TRUE,  QSP_TRUE, QSP_TRUE },
    /* NUMBER */ { QSP_TRUE, QSP_TRUE, QSP_TRUE,  QSP_TRUE, QSP_TRUE },
    /* STRING */ { QSP_TRUE, QSP_TRUE, QSP_TRUE,  QSP_TRUE, QSP_TRUE },
    /* CODE */   { QSP_TRUE, QSP_TRUE, QSP_FALSE, QSP_TRUE, QSP_TRUE },
    /* VARREF */ { QSP_TRUE, QSP_TRUE, QSP_FALSE, QSP_TRUE, QSP_TRUE },
    /* UNDEF */  { QSP_TRUE, QSP_TRUE, QSP_TRUE,  QSP_TRUE, QSP_TRUE },
};

INLINE void qspFormatVariant(QSPVariant *val);
INLINE QSP_BOOL qspSumSimpleVariants(QSPVariant *arg1, QSPVariant *arg2, QSPVariant *res);

INLINE void qspFormatVariant(QSPVariant *val)
{
    switch (val->Type)
    {
    case QSP_TYPE_VARREF:
        {
            QSPString temp = qspCopyToNewText(qspDelSpc(QSP_PSTR(val)));
            qspUpperStr(&temp);
            qspFreeString(&QSP_PSTR(val));
            QSP_PSTR(val) = temp;
            break;
        }
    }
}

INLINE QSP_BOOL qspSumSimpleVariants(QSPVariant *arg1, QSPVariant *arg2, QSPVariant *res)
{
    switch (QSP_BASETYPE(arg1->Type))
    {
    case QSP_TYPE_TUPLE:
        return QSP_FALSE; /* tuples get processed before calling this function */
    case QSP_TYPE_STR:
        switch (QSP_BASETYPE(arg2->Type))
        {
        case QSP_TYPE_TUPLE:
            return QSP_FALSE; /* tuples get processed before calling this function */
        case QSP_TYPE_STR:
            qspAddText(&QSP_PSTR(res), QSP_PSTR(arg1), QSP_TRUE);
            qspAddText(&QSP_PSTR(res), QSP_PSTR(arg2), QSP_FALSE);
            res->Type = QSP_TYPE_STR;
            break;
        case QSP_TYPE_NUM:
            if (qspCanConvertToNum(arg1))
            {
                qspConvertVariantTo(arg1, QSP_TYPE_NUM);
                *res = qspNumVariant(QSP_PNUM(arg1) + QSP_PNUM(arg2));
            }
            else
            {
                qspConvertVariantTo(arg2, QSP_TYPE_STR);
                qspAddText(&QSP_PSTR(res), QSP_PSTR(arg1), QSP_TRUE);
                qspAddText(&QSP_PSTR(res), QSP_PSTR(arg2), QSP_FALSE);
                res->Type = QSP_TYPE_STR;
            }
            break;
        }
        break;
    case QSP_TYPE_NUM:
        switch (QSP_BASETYPE(arg2->Type))
        {
        case QSP_TYPE_TUPLE:
            return QSP_FALSE; /* tuples get processed before calling this function */
        case QSP_TYPE_STR:
            if (qspCanConvertToNum(arg2))
            {
                qspConvertVariantTo(arg2, QSP_TYPE_NUM);
                *res = qspNumVariant(QSP_PNUM(arg1) + QSP_PNUM(arg2));
            }
            else
            {
                qspConvertVariantTo(arg1, QSP_TYPE_STR);
                qspAddText(&QSP_PSTR(res), QSP_PSTR(arg1), QSP_TRUE);
                qspAddText(&QSP_PSTR(res), QSP_PSTR(arg2), QSP_FALSE);
                res->Type = QSP_TYPE_STR;
            }
            break;
        case QSP_TYPE_NUM:
            *res = qspNumVariant(QSP_PNUM(arg1) + QSP_PNUM(arg2));
            break;
        }
        break;
    }
    return QSP_TRUE;
}

QSPString qspGetVariantAsString(QSPVariant *val)
{
    switch (QSP_BASETYPE(val->Type))
    {
    case QSP_TYPE_TUPLE:
        return qspGetTupleAsString(QSP_PTUPLE(val));
    case QSP_TYPE_NUM:
        {
            QSP_CHAR buf[QSP_MAX_BIGINT_LEN];
            return qspCopyToNewText(qspNumToStr(buf, QSP_PNUM(val)));
        }
    case QSP_TYPE_STR:
        return qspCopyToNewText(QSP_PSTR(val));
    }
    return qspNullString;
}

QSP_BIGINT qspGetVariantAsNum(QSPVariant *val, QSP_BOOL *isValid)
{
    switch (QSP_BASETYPE(val->Type))
    {
    case QSP_TYPE_TUPLE:
        return qspTupleToNum(QSP_PTUPLE(val), isValid);
    case QSP_TYPE_NUM:
        if (isValid) *isValid = QSP_TRUE;
        return QSP_PNUM(val);
    case QSP_TYPE_STR:
        return qspStrToNum(QSP_PSTR(val), isValid);
    }
    if (isValid) *isValid = QSP_FALSE; /* shouldn't happen */
    return 0;
}

QSP_BOOL qspConvertVariantTo(QSPVariant *val, QSP_TINYINT type)
{
    /* NB: val->Type can be QSP_TYPE_UNDEF */
    if (val->Type != type && qspTypeConversionTable[val->Type][type])
    {
        QSP_TINYINT toBaseType = QSP_BASETYPE(type);
        if (QSP_BASETYPE(val->Type) != toBaseType)
        {
            switch (toBaseType)
            {
            case QSP_TYPE_TUPLE:
                QSP_PTUPLE(val) = qspMoveToNewTuple(val, 1);
                break;
            case QSP_TYPE_NUM:
                {
                    QSP_BOOL isValid;
                    QSP_BIGINT result = qspGetVariantAsNum(val, &isValid);
                    if (!isValid) return QSP_FALSE;
                    qspFreeVariant(val);
                    QSP_PNUM(val) = result;
                    break;
                }
            case QSP_TYPE_STR:
                {
                    QSPString result = qspGetVariantAsString(val);
                    qspFreeVariant(val);
                    QSP_PSTR(val) = result;
                    break;
                }
            }
        }
        val->Type = type;
        qspFormatVariant(val);
    }
    return QSP_TRUE;
}

int qspVariantsCompare(QSPVariant *first, QSPVariant *second)
{
    switch (QSP_BASETYPE(first->Type))
    {
    case QSP_TYPE_TUPLE:
        return qspTupleValueCompare(QSP_PTUPLE(first), second);
    case QSP_TYPE_NUM:
        switch (QSP_BASETYPE(second->Type))
        {
        case QSP_TYPE_TUPLE:
            return -qspTupleValueCompare(QSP_PTUPLE(second), first);
        case QSP_TYPE_NUM:
            return (QSP_PNUM(first) > QSP_PNUM(second) ? 1 : (QSP_PNUM(first) < QSP_PNUM(second) ? -1 : 0));
        case QSP_TYPE_STR:
            {
                QSP_BOOL isValid;
                QSP_BIGINT num = qspStrToNum(QSP_PSTR(second), &isValid);
                if (!isValid)
                {
                    QSP_CHAR buf[QSP_MAX_BIGINT_LEN];
                    return qspStrsCompare(qspNumToStr(buf, QSP_PNUM(first)), QSP_PSTR(second));
                }
                return (QSP_PNUM(first) > num ? 1 : (QSP_PNUM(first) < num ? -1 : 0));
            }
        }
        break;
    case QSP_TYPE_STR:
        switch (QSP_BASETYPE(second->Type))
        {
        case QSP_TYPE_TUPLE:
            return -qspTupleValueCompare(QSP_PTUPLE(second), first);
        case QSP_TYPE_NUM:
            {
                QSP_BOOL isValid;
                QSP_BIGINT num = qspStrToNum(QSP_PSTR(first), &isValid);
                if (!isValid)
                {
                    QSP_CHAR buf[QSP_MAX_BIGINT_LEN];
                    return qspStrsCompare(QSP_PSTR(first), qspNumToStr(buf, QSP_PNUM(second)));
                }
                return (num > QSP_PNUM(second) ? 1 : (num < QSP_PNUM(second) ? -1 : 0));
            }
        case QSP_TYPE_STR:
            return qspStrsCompare(QSP_PSTR(first), QSP_PSTR(second));
        }
        break;
    }
    return 0;
}

void qspAutoConvertAppend(QSPVariant *arg1, QSPVariant *arg2, QSPVariant *res)
{
    switch (QSP_BASETYPE(arg1->Type))
    {
    case QSP_TYPE_TUPLE:
        switch (QSP_BASETYPE(arg2->Type))
        {
        case QSP_TYPE_TUPLE:
            *res = qspTupleVariant(qspMergeToNewTuple(QSP_PTUPLE(arg1).Vals, QSP_PTUPLE(arg1).Items, QSP_PTUPLE(arg2).Vals, QSP_PTUPLE(arg2).Items));
            break;
        case QSP_TYPE_NUM:
        case QSP_TYPE_STR:
            *res = qspTupleVariant(qspMergeToNewTuple(QSP_PTUPLE(arg1).Vals, QSP_PTUPLE(arg1).Items, arg2, 1));
            break;
        }
        break;
    case QSP_TYPE_NUM:
    case QSP_TYPE_STR:
        switch (QSP_BASETYPE(arg2->Type))
        {
        case QSP_TYPE_TUPLE:
            *res = qspTupleVariant(qspMergeToNewTuple(arg1, 1, QSP_PTUPLE(arg2).Vals, QSP_PTUPLE(arg2).Items));
            break;
        case QSP_TYPE_NUM:
        case QSP_TYPE_STR:
            qspConvertVariantTo(arg1, QSP_TYPE_STR);
            qspConvertVariantTo(arg2, QSP_TYPE_STR);

            qspAddText(&QSP_PSTR(res), QSP_PSTR(arg1), QSP_TRUE);
            qspAddText(&QSP_PSTR(res), QSP_PSTR(arg2), QSP_FALSE);
            res->Type = QSP_TYPE_STR;
            break;
        }
        break;
    }
}

QSP_BOOL qspAutoConvertCombine(QSPVariant *arg1, QSPVariant *arg2, QSP_CHAR op, QSPVariant *res)
{
    if (QSP_BASETYPE(arg1->Type) == QSP_TYPE_TUPLE)
    {
        QSPTuple *tuple = &QSP_PTUPLE(arg1);
        if (tuple->Vals)
        {
            int i;
            QSPVariant *vals = (QSPVariant *)malloc(tuple->Items * sizeof(QSPVariant));
            for (i = 0; i < tuple->Items; ++i)
            {
                if (!qspAutoConvertCombine(tuple->Vals + i, arg2, op, vals + i))
                {
                    qspFreeVariants(vals, i);
                    free(vals);
                    return QSP_FALSE;
                }
            }
            QSP_PTUPLE(res).Vals = vals;
            QSP_PTUPLE(res).Items = tuple->Items;
        }
        else
        {
            QSP_PTUPLE(res).Vals = 0;
            QSP_PTUPLE(res).Items = 0;
        }
        res->Type = QSP_TYPE_TUPLE;
        return QSP_TRUE;
    }
    if (QSP_BASETYPE(arg2->Type) == QSP_TYPE_TUPLE)
    {
        QSPTuple *tuple = &QSP_PTUPLE(arg2);
        if (tuple->Vals)
        {
            int i;
            QSPVariant *vals = (QSPVariant *)malloc(tuple->Items * sizeof(QSPVariant));
            for (i = 0; i < tuple->Items; ++i)
            {
                if (!qspAutoConvertCombine(arg1, tuple->Vals + i, op, vals + i))
                {
                    qspFreeVariants(vals, i);
                    free(vals);
                    return QSP_FALSE;
                }
            }
            QSP_PTUPLE(res).Vals = vals;
            QSP_PTUPLE(res).Items = tuple->Items;
        }
        else
        {
            QSP_PTUPLE(res).Vals = 0;
            QSP_PTUPLE(res).Items = 0;
        }
        res->Type = QSP_TYPE_TUPLE;
        return QSP_TRUE;
    }

    if (op == QSP_ADD[0])
        return qspSumSimpleVariants(arg1, arg2, res);

    if (!qspConvertVariantTo(arg1, QSP_TYPE_NUM) || !qspConvertVariantTo(arg2, QSP_TYPE_NUM))
    {
        qspSetError(QSP_ERR_TYPEMISMATCH);
        return QSP_FALSE;
    }

    if (op == QSP_SUB[0])
    {
        QSP_PNUM(res) = QSP_PNUM(arg1) - QSP_PNUM(arg2);
    }
    else if (op == QSP_MUL[0])
    {
        QSP_PNUM(res) = QSP_PNUM(arg1) * QSP_PNUM(arg2);
    }
    else /* QSP_DIV */
    {
        if (QSP_PNUM(arg2) == 0)
        {
            qspSetError(QSP_ERR_DIVBYZERO);
            return QSP_FALSE;
        }
        QSP_PNUM(res) = QSP_PNUM(arg1) / QSP_PNUM(arg2);
    }
    res->Type = QSP_TYPE_NUM;
    return QSP_TRUE;
}

void qspAppendVariantToIndexString(QSPVariant *val, QSPBufString *res)
{
    QSP_CHAR buf[QSP_MAX_BIGINT_LEN];
    switch (QSP_BASETYPE(val->Type))
    {
    case QSP_TYPE_TUPLE:
        {
            int items = QSP_PTUPLE(val).Items;
            qspAddBufText(res, qspNumToStr(buf, items));
            if (items > 0)
            {
                QSPVariant *item = QSP_PTUPLE(val).Vals;
                qspAddBufText(res, QSP_STATIC_STR(QSP_IND_DELIM));
                while (--items > 0)
                {
                    qspAppendVariantToIndexString(item, res);
                    qspAddBufText(res, QSP_STATIC_STR(QSP_IND_DELIM));
                    ++item;
                }
                qspAppendVariantToIndexString(item, res);
            }
            break;
        }
    case QSP_TYPE_NUM:
        qspAddBufText(res, QSP_STATIC_STR(QSP_NUMCHAR)); /* type id */
        qspAddBufText(res, qspNumToStr(buf, QSP_PNUM(val)));
        break;
    case QSP_TYPE_STR:
        qspAddBufText(res, QSP_STATIC_STR(QSP_STRCHAR)); /* type id */
        qspAddBufText(res, QSP_PSTR(val));
        break;
    }
}
