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

#include "qsp_config.h"

#ifndef QSP_GAMEDEFINES
    #define QSP_GAMEDEFINES

    #define QSP_GAMEID QSP_FMT("QSPGAME")
    #define QSP_SAVEDGAMEID QSP_FMT("QSPSAVEDGAME")
    #define QSP_GAMEMINVER QSP_FMT(QSP_GAMEMINVER_STR)
    #define QSP_MAXINCFILES 100
    #define QSP_DEFTIMERINTERVAL 500
    #define QSP_SAVEDGAMEDATAEXTRASPACE 8192

    extern int qspQstCRC;
    extern int qspCurIncLocsCount;

    /* External functions */
    void qspClearAllIncludes(QSP_BOOL toInit);
    QSP_BOOL qspNewGame(QSP_BOOL toReset);
    QSP_BOOL qspOpenGame(void *data, int dataSize, QSP_BOOL isNewGame);
    QSP_BOOL qspSaveGameStatus(void *buf, int *bufSize, QSP_BOOL isUCS);
    QSP_BOOL qspOpenGameStatus(void *data, int dataSize);
    /* Statements */
    void qspStatementOpenQst(QSPVariant *args, QSP_TINYINT count, QSP_TINYINT extArg);
    void qspStatementOpenGame(QSPVariant *args, QSP_TINYINT count, QSP_TINYINT extArg);
    void qspStatementSaveGame(QSPVariant *args, QSP_TINYINT count, QSP_TINYINT extArg);

#endif
