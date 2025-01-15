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
#include "codetools.h"

#ifndef QSP_CALLSDEFINES
    #define QSP_CALLSDEFINES

    typedef struct
    {
        int LocationState;
        QSP_BOOL IsInCallback;
        QSP_BOOL IsMainDescChanged;
        QSP_BOOL IsVarsDescChanged;
        QSP_BOOL IsObjsListChanged;
        QSP_BOOL IsActsListChanged;
        int RealCurLoc;
        int RealActIndex;
        int RealLineNum;
        QSPLineOfCode *RealLine;
    } QSPCallState;

    extern QSP_CALLBACK qspCallbacks[QSP_CALL_DUMMY];
    extern QSP_BOOL qspIsInCallback;

    /* External functions */
    void qspPrepareCallback(QSPCallState *state, QSP_BOOL toRefreshUI);
    QSP_BOOL qspFinalizeCallback(QSPCallState *state, QSP_BOOL toResetLocationState);

    void qspInitCallbacks(void);
    void qspSetCallback(int type, QSP_CALLBACK func);

    void qspCallDebug(QSPString str);
    void qspCallSetTimer(int msecs);
    void qspCallRefreshInt(QSP_BOOL isForced);
    void qspCallSetInputStrText(QSPString text);
    void qspCallShowMessage(QSPString text);
    void qspCallShowPicture(QSPString file);
    void qspCallShowWindow(int type, QSP_BOOL toShow);
    void qspCallPlayFile(QSPString file, int volume);
    QSP_BOOL qspCallIsPlayingFile(QSPString file);
    void qspCallCloseFile(QSPString file);
    void qspCallSystem(QSPString cmd);
    void qspCallSleep(int msecs);
    int qspCallGetMSCount(void);
    void qspCallOpenGame(QSPString file, QSP_BOOL isNewGame);
    void qspCallOpenGameStatus(QSPString file);
    void qspCallSaveGameStatus(QSPString file);
    QSPString qspCallInputBox(QSPString text);
    int qspCallShowMenu(QSPListItem *items, int count);
    QSPString qspCallVersion(QSPString param);

#endif
