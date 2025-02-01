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

#include "declarations.h"
#include "variant.h"

#ifndef QSP_MATHDEFINES
    #define QSP_MATHDEFINES

    #define QSP_OPSLEVELS 2
    #define QSP_MAXOPSNAMES 150
    #define QSP_OPMAXARGS 20
    #define QSP_STACKSIZE 30
    #define QSP_MAXITEMS 200
    #define QSP_CACHEDEXPSMAXBUCKETSIZE 5
    #define QSP_CACHEDEXPSBUCKETS 512

    /* Helpers */
    #define QSP_TOBOOL(x) ((x) != 0) /* converts a number to a QSP boolean value */
    #define QSP_ISTRUE(x) ((x) != 0) /* checks whether a QSP numeric value represents boolean 'true' */
    #define QSP_ISFALSE(x) ((x) == 0) /* checks whether a QSP numeric value represents boolean 'false' */

    typedef void (*QSP_FUNCTION)(QSPVariant *args, QSP_TINYINT argsCount, QSPVariant *res);

    typedef struct
    {
        QSP_TINYINT Code;
        QSPString Name;
    } QSPMathOpName;

    typedef struct
    {
        QSP_TINYINT Priority;
        QSP_TINYINT ResType;
        QSP_TINYINT MinArgsCount;
        QSP_TINYINT MaxArgsCount;
        QSP_TINYINT ArgsTypes[QSP_OPMAXARGS];
        QSP_FUNCTION Func;
    } QSPMathOperation;

    typedef struct
    {
        QSP_TINYINT OpCode;
        QSP_TINYINT ArgsCount;
        QSPVariant Value;
    } QSPMathCompiledOp;

    typedef struct
    {
        QSPMathCompiledOp *CompItems;
        int ItemsCount;
        int Capacity;
    } QSPMathExpression;

    typedef struct
    {
        QSPString Text;
        QSPMathExpression CompiledExp;
    } QSPCachedMathExp;

    typedef struct
    {
        QSPCachedMathExp Exps[QSP_CACHEDEXPSMAXBUCKETSIZE];
        int ExpsCount;
        int ExpToEvict;
    } QSPCachedMathExpsBucket;

    enum
    {
        qspOpUnknown,
        qspOpStart, /* sequence point */
        qspOpEnd, /* sequence point */
        qspOpOpenRoundBracket, /* sequence point */
        qspOpCloseRoundBracket, /* sequence point */
        qspOpOpenSquareBracket, /* sequence point */
        qspOpCloseSquareBracket, /* sequence point */
        qspOpComma, /* sequence point */
        qspOpTuple,
        qspOpValue,
        qspOpValueToFormat,
        qspOpNegation,
        qspOpAppend,
        qspOpAdd,
        qspOpSub,
        qspOpMul,
        qspOpDiv,
        qspOpMod,
        qspOpAnd,
        qspOpOr,
        qspOpNe,
        qspOpLeq,
        qspOpGeq,
        qspOpEq,
        qspOpLt,
        qspOpGt,

        qspOpFirst_Function,
        qspOpNot = qspOpFirst_Function,
        qspOpIIf,
        qspOpMin,
        qspOpMax,
        qspOpRand,
        qspOpRnd,
        qspOpArrSize,
        qspOpArrType,
        qspOpArrItem,
        qspOpLastArrItem,
        qspOpArrPack,
        qspOpArrPos,
        qspOpArrComp,
        qspOpStr,
        qspOpVal,
        qspOpIsNum,
        qspOpLen,
        qspOpLCase,
        qspOpUCase,
        qspOpTrim,
        qspOpMid,
        qspOpInstr,
        qspOpReplace,
        qspOpStrComp,
        qspOpStrFind,
        qspOpStrPos,
        qspOpFunc,
        qspOpDynEval,
        qspOpLoc,
        qspOpCurLoc,
        qspOpDesc,
        qspOpMainText,
        qspOpStatText,
        qspOpUserText,
        qspOpInput,
        qspOpObj,
        qspOpSelObj,
        qspOpGetObj,
        qspOpCountObj,
        qspOpCurObjs,
        qspOpSelAct,
        qspOpCurActs,
        qspOpRGB,
        qspOpIsPlay,
        qspOpMsecsCount,
        qspOpQSPVer,

        qspOpLast_Operation
    };

    /* External functions */
    void qspInitMath(void);
    void qspTerminateMath(void);
    void qspClearAllMathExps(QSP_BOOL toInit);
    QSP_BOOL qspCompileMathExpression(QSPString s, QSPMathExpression *expression);
    void qspFreeMathExpression(QSPMathExpression *expression);
    QSPVariant qspCalculateValue(QSPMathExpression *expression, int valueIndex);
    QSPVariant qspCalculateExprValue(QSPString expr);

#endif
