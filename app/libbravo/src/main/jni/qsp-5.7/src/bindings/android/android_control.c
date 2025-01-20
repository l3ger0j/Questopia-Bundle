/* Copyright (C) 2005-2010 Valeriy Argunov (nporep AT mail DOT ru) */
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

#include "../../declarations.h"

#ifdef _ANDROID

#include "../../actions.h"
#include "../../callbacks.h"
#include "../../coding.h"
#include "../../common.h"
#include "../../errors.h"
#include "../../game.h"
#include "../../locations.h"
#include "../../mathops.h"
#include "../../menu.h"
#include "../../objects.h"
#include "../../statements.h"
#include "../../text.h"
#include "../../time.h"
#include "../../variables.h"
#include "../../variant.h"

JavaVM *ndkJvm;
jclass ndkApiClass;
jobject ndkApiObject;

jclass ndkListItemClass;
jclass ndkExecutionStateClass;
jclass ndkErrorInfoClass;
jclass ndkVarValResp;

/* ------------------------------------------------------------ */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsInCallBack(JNIEnv *env, jobject this)
{
	return qspIsInCallBack;
}
/* ------------------------------------------------------------ */
/* Debugging */

/* Managing the debugging mode */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPEnableDebugMode(JNIEnv *env, jobject this, jboolean isDebug)
{
	qspIsDebug = isDebug;
}

/* Getting current state data */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetCurStateData(JNIEnv *env, jobject this)
{
	//!!!STUB
//	*loc = (qspRealCurLoc >= 0 && qspRealCurLoc < qspLocsCount ? qspLocs[qspRealCurLoc].Name : 0);
//	*actIndex = qspRealActIndex;
//	*line = qspRealLine;
	return NULL;
}
/* ------------------------------------------------------------ */
/* Version Information */

/* Version */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVersion(JNIEnv *env, jobject this)
{
	char *sz = qspW2C(QSP_VER);
	jstring result = (*env)->NewStringUTF(env, sz);
	if (sz != NULL)
		free(sz);
	return result;
}

/* Date and time of compilation */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetCompiledDateTime(JNIEnv *env, jobject this)
{
	char *sz = qspW2C(QSP_FMT(__DATE__) QSP_FMT(", ") QSP_FMT(__TIME__));
	jstring result = (*env)->NewStringUTF(env, sz);
	if (sz != NULL)
		free(sz);
	return result;
}
/* ------------------------------------------------------------ */
/* Number of full location updates */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetFullRefreshCount(JNIEnv *env, jobject this)
{
	return qspFullRefreshCount;
}
/* ------------------------------------------------------------ */
/* Full path to the downloaded game file */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetQstFullPath(JNIEnv *env, jobject this)
{
	char *sz = qspW2C(qspQstFullPath);
	jstring result = (*env)->NewStringUTF(env, sz);
	if (sz != NULL)
		free(sz);
	return result;
}
/* ------------------------------------------------------------ */
/* Name of the current location */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetCurLoc(JNIEnv *env, jobject this)
{
	char *sz = qspW2C((qspCurLoc >= 0 ? qspLocs[qspCurLoc].Name : 0));
	jstring result = (*env)->NewStringUTF(env, sz);
	if (sz != NULL)
		free(sz);
	return result;
}
/* ------------------------------------------------------------ */
/* Basic description of the location */

/* Text of the main location description window */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetMainDesc(JNIEnv *env, jobject this)
{
	char *sz = qspW2C(qspCurDesc);
	jstring result = (*env)->NewStringUTF(env, sz);
	if (sz != NULL)
		free(sz);
	return result;
}

/* The ability to change the text of the main description */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsMainDescChanged(JNIEnv *env, jobject this)
{
	return qspIsMainDescChanged;
}
/* ------------------------------------------------------------ */
/* Additional description of the location */

/* Text of the additional location description window */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVarsDesc(JNIEnv *env, jobject this)
{
	char *sz = qspW2C(qspCurVars);
	jstring result = (*env)->NewStringUTF(env, sz);
	if (sz != NULL)
		free(sz);
	return result;
}

/* Possibility to change the text of the additional description */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsVarsDescChanged(JNIEnv *env, jobject this)
{
	return qspIsVarsDescChanged;
}

/* ------------------------------------------------------------ */
/* Get the value of the specified expression */
//(const QSP_CHAR *expr, QSP_BOOL *isString, int *numVal, QSP_CHAR *strVal, int strValBufSize)
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetExprValue(JNIEnv *env, jobject this)
{
	//!!!STUB
	//{
	//	QSPVariant v;
	//	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	//	qspResetError();
	//	if (qspIsDisableCodeExec) return QSP_FALSE;
	//	v = qspExprValue((QSP_CHAR *)expr);
	//	if (qspErrorNum) return QSP_FALSE;
	//	*isString = v.IsStr;
	//	if (v.IsStr)
	//	{
	//		qspStrNCopy(strVal, QSP_STR(v), strValBufSize - 1);
	//		free(QSP_STR(v));
	//		strVal[strValBufSize - 1] = 0;
	//	}
	//	else
	//		*numVal = QSP_NUM(v);
	//	return QSP_TRUE;
	//}
	return NULL;
}
/* ------------------------------------------------------------ */
/* Text of the input line */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSetInputStrText(JNIEnv *env, jobject this, jstring val)
{
	const char *str = (*env)->GetStringUTFChars(env, val, NULL);
	if (str == NULL)
		return;
	QSP_CHAR *strConverted = qspC2W(str);

	qspCurInputLen = qspAddText(&strConverted, (QSP_CHAR *)val, 0, -1, QSP_FALSE);

	(*env)->ReleaseStringUTFChars(env, val, str);
}
/* ------------------------------------------------------------ */
/* List of actions */

/* Number of actions */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetActionsCount(JNIEnv *env, jobject this)
{
	return qspCurActionsCount;
}

/* Data actions with the specified index */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetActionData(JNIEnv *env, jobject this, jint ind)
{
	QSP_CHAR *qspImgFileName;
	QSP_CHAR *qspActName;

	if (ind >= 0 && ind < qspCurActionsCount)
	{
		qspImgFileName = qspCurActions[ind].Image;
		qspActName = qspCurActions[ind].Desc;
	}
	else
	{
		qspImgFileName = qspActName = 0;
	}

	jstring actName = qspToJVMString(env, qspActName);
	jstring actImg = qspToJVMString(env, qspImgFileName);

	if (ndkListItemClass == 0)
		return 0;

	jobject obj = (*env)->AllocObject(env, ndkListItemClass);
	jfieldID fid = (*env)->GetFieldID(env, ndkListItemClass, "text", "Ljava/lang/String;");
	jfieldID fid2 = (*env)->GetFieldID(env, ndkListItemClass, "pathToImage", "Ljava/lang/String;");

	if (fid == 0 || fid2 == 0)
		return 0;

	(*env)->SetObjectField(env, obj, fid, actName);
	(*env)->SetObjectField(env, obj, fid2, actImg);

	return obj;
}

/* Executing the code of the selected action */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecuteSelActionCode(JNIEnv *env, jobject this, jboolean isRefresh)
{
	if (qspCurSelAction >= 0)
	{
		if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
		qspPrepareExecution();
		if (qspIsDisableCodeExec) return QSP_FALSE;
		qspExecAction(qspCurSelAction);
		if (qspErrorNum) return QSP_FALSE;
		if ((QSP_BOOL)isRefresh) qspCallRefreshInt(QSP_FALSE);
	}
	return QSP_TRUE;
}

/* Set the index of the selected action */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSetSelActionIndex(JNIEnv *env, jobject this, jint ind, jboolean isRefresh)
{
	if (ind >= 0 && ind < qspCurActionsCount && ind != qspCurSelAction)
	{
		if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
		qspPrepareExecution();
		if (qspIsDisableCodeExec) return QSP_FALSE;
		qspCurSelAction = ind;
		qspExecLocByVarNameWithArgs(QSP_FMT("ONACTSEL"), 0, 0);
		if (qspErrorNum) return QSP_FALSE;
		if ((QSP_BOOL)isRefresh) qspCallRefreshInt(QSP_FALSE);
	}
	return QSP_TRUE;
}

/* Get the index of the selected action */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetSelActionIndex(JNIEnv *env, jobject this)
{
	return qspCurSelAction;
}

/* Ability to change the list of actions */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsActionsChanged(JNIEnv *env, jobject this)
{
	return qspIsActionsChanged;
}
/* ------------------------------------------------------------ */
/* List of objects */

/* Number of objects */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetObjectsCount(JNIEnv *env, jobject this)
{
	return qspCurObjectsCount;
}

/* Object data with the specified index */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetObjectData(JNIEnv *env, jobject this, jint ind)
{
	QSP_CHAR *ndkImgFileName;
	QSP_CHAR *ndkObjName;

	if (ind >= 0 && ind < qspCurObjectsCount)
	{
		ndkImgFileName = qspCurObjects[ind].Image;
		ndkObjName = qspCurObjects[ind].Desc;
	}
	else
	{
		ndkImgFileName = ndkObjName = 0;
	}

	jstring objName = qspToJVMString(env, ndkObjName);
	jstring objImg = qspToJVMString(env, ndkImgFileName);

	if (ndkListItemClass == 0)
		return 0;

	jobject obj = (*env)->AllocObject(env, ndkListItemClass);
	jfieldID fid = (*env)->GetFieldID(env, ndkListItemClass, "text", "Ljava/lang/String;");
	jfieldID fid2 = (*env)->GetFieldID(env, ndkListItemClass, "pathToImage", "Ljava/lang/String;");

	if (fid == 0 || fid2 == 0)
		return 0;

	// Set the major field to the operating system's major version.
	(*env)->SetObjectField(env, obj, fid, objName);
	(*env)->SetObjectField(env, obj, fid2, objImg);

	return obj;
}

/* Set the index of the selected object */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSetSelObjectIndex(JNIEnv *env, jobject this, jint ind, jboolean isRefresh)
{
	if (ind >= 0 && ind < qspCurObjectsCount && ind != qspCurSelObject)
	{
		if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
		qspPrepareExecution();
		if (qspIsDisableCodeExec) return QSP_FALSE;
		qspCurSelObject = ind;
		qspExecLocByVarNameWithArgs(QSP_FMT("ONOBJSEL"), 0, 0);
		if (qspErrorNum) return QSP_FALSE;
		if ((QSP_BOOL)isRefresh) qspCallRefreshInt(QSP_FALSE);
	}
	return QSP_TRUE;
}

/* Get the index of the selected object */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetSelObjectIndex(JNIEnv *env, jobject this)
{
	return qspCurSelObject;
}

/* Ability to change the list of objects */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsObjectsChanged(JNIEnv *env, jobject this)
{
	return qspIsObjectsChanged;
}
/* ------------------------------------------------------------ */
/* Show/hide windows */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPShowWindow(JNIEnv *env, jobject this, jint type, jboolean isShow)
{
	QSP_BOOL mIsShow = (QSP_BOOL)isShow;
	switch (type)
	{
	case QSP_WIN_ACTS:
		qspCurIsShowActs = mIsShow;
		break;
	case QSP_WIN_OBJS:
		qspCurIsShowObjs = mIsShow;
		break;
	case QSP_WIN_VARS:
		qspCurIsShowVars = mIsShow;
		break;
	case QSP_WIN_INPUT:
		qspCurIsShowInput = mIsShow;
		break;
	}
}
/* ------------------------------------------------------------ */
/* Variables */

/* Get the number of array elements */
//QSP_BOOL QSPGetVarValuesCount(const QSP_CHAR *name, int *count)
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVarValuesCount(JNIEnv *env, jobject this, jstring name)
{
	//!!!STUB
	//{
	//	QSPVar *var;
	//	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	//	qspResetError();
	//	var = qspVarReference((QSP_CHAR *)name, QSP_FALSE);
	//	if (qspErrorNum) return QSP_FALSE;
	//	*count = var->ValsCount;
	//	return QSP_TRUE;
	//}
	return NULL;
}

/* Get the values of the specified array element */
//QSP_BOOL QSPGetVarValues(const QSP_CHAR *name, int ind, int *numVal, QSP_CHAR **strVal)
QSP_BOOL QSPGetVarValues(const QSP_CHAR *name, int ind, int *numVal, QSP_CHAR **strVal)
{
	QSPVar *var;
	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspResetError();
	var = qspVarReference((QSP_CHAR *)name, QSP_FALSE);
	if (qspErrorNum || ind < 0 || ind >= var->ValsCount) return QSP_FALSE;
	*numVal = var->Values[ind].Num;
	*strVal = var->Values[ind].Str;
	return QSP_TRUE;
}

JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVarValues(JNIEnv *env, jobject this, jstring name, jint ind)
{
	//Convert array name to QSP string
	const char *str = (*env)->GetStringUTFChars(env, name, NULL);
	if (str == NULL)
		return NULL;
	QSP_CHAR *strConverted = qspC2W(str);

	//Call QSP function
	int numVal = 0;
	char *strVal;
	QSP_BOOL result = QSPGetVarValues(strConverted, (int)ind, &numVal, &strVal);

	// If this class does not exist then return null.
	if (ndkVarValResp == 0)
		return NULL;
	jobject obj = (*env)->AllocObject(env, ndkVarValResp);

	jfieldID successFid = (*env)->GetFieldID(env, ndkVarValResp, "isSuccess", "Z");
	if (successFid == 0)
		return NULL;
	if (result == QSP_TRUE) {
		(*env)->SetBooleanField(env, obj, successFid, JNI_TRUE);

		char *sz = qspW2C(strVal);
		jstring jstringVal = (*env)->NewStringUTF(env, sz);
		if (sz != NULL)
			free(sz);

		jfieldID stringValueFid = (*env)->GetFieldID(env, ndkVarValResp, "stringValue", "Ljava/lang/String;");
		if (stringValueFid == 0)
			return NULL;
		(*env)->SetObjectField(env, obj, stringValueFid, jstringVal);

		jfieldID intValueFid = (*env)->GetFieldID(env, ndkVarValResp, "intValue", "I");
		if (intValueFid == 0)
			return NULL;
		(*env)->SetIntField(env, obj, intValueFid, numVal);
	} else {
		(*env)->SetBooleanField(env, obj, successFid, JNI_FALSE);
	}

	(*env)->ReleaseStringUTFChars(env, name, str);
	return obj;
}

/* Get the maximum number of variables */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetMaxVarsCount(JNIEnv *env, jobject this)
{
	return QSP_VARSCOUNT;
}

/* Get the variable name with the specified index */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVarNameByIndex(JNIEnv *env, jobject this, jint index)
{
	//!!!STUB
//{
//	if (index < 0 || index >= QSP_VARSCOUNT || !qspVars[index].Name) return QSP_FALSE;
//	*name = qspVars[index].Name;
//	return QSP_TRUE;
//}
	return NULL;
}
/* ------------------------------------------------------------ */
/* Code Execution */

/* Executing a line of code */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecString(JNIEnv *env, jobject this, jstring s, jboolean isRefresh)
{
	const char *str = (*env)->GetStringUTFChars(env, s, NULL);
	if (str == NULL)
		return JNI_FALSE;
	QSP_CHAR *strConverted = qspC2W(str);

	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspPrepareExecution();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	qspExecStringAsCodeWithArgs((QSP_CHAR *)strConverted, 0, 0);
	if (qspErrorNum) return QSP_FALSE;
	if ((QSP_BOOL)isRefresh) qspCallRefreshInt(QSP_FALSE);

	(*env)->ReleaseStringUTFChars(env, s, str);
	return QSP_TRUE;
}

/* Executing the code of the specified location */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecLocationCode(JNIEnv *env, jobject this, jstring name, jboolean isRefresh)
{
	const char *str = (*env)->GetStringUTFChars(env, name, NULL);
	if (str == NULL)
		return JNI_FALSE;
	QSP_CHAR *strConverted = qspC2W(str);

	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspPrepareExecution();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	qspExecLocByName((QSP_CHAR *)strConverted, QSP_FALSE);
	if (qspErrorNum) return QSP_FALSE;
	if ((QSP_BOOL)isRefresh) qspCallRefreshInt(QSP_FALSE);

	(*env)->ReleaseStringUTFChars(env, name, str);
	return QSP_TRUE;
}

/* Execution of the location-counter code */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecCounter(JNIEnv *env, jobject this, jboolean isRefresh)
{
	if (!qspIsInCallBack)
	{
		qspPrepareExecution();
		qspExecLocByVarNameWithArgs(QSP_FMT("COUNTER"), 0, 0);
		if (qspErrorNum) return QSP_FALSE;
		if ((QSP_BOOL)isRefresh) qspCallRefreshInt(QSP_FALSE);
	}
	return QSP_TRUE;
}

/* Execution of the code of the input line handler location */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecUserInput(JNIEnv *env, jobject this, jboolean isRefresh)
{
	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspPrepareExecution();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	qspExecLocByVarNameWithArgs(QSP_FMT("USERCOM"), 0, 0);
	if (qspErrorNum) return QSP_FALSE;
	if ((QSP_BOOL)isRefresh) qspCallRefreshInt(QSP_FALSE);
	return QSP_TRUE;
}
/* ------------------------------------------------------------ */
/* Errors */

/* Get information about the latest error */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetLastErrorData(JNIEnv *env, jobject this)
{
	if (ndkErrorInfoClass == 0)
		return NULL;

	jfieldID fid = (*env)->GetFieldID(env, ndkErrorInfoClass, "locName", "Ljava/lang/String;");
	jfieldID fid2 = (*env)->GetFieldID(env, ndkErrorInfoClass, "errorNum", "I");
	jfieldID fid3 = (*env)->GetFieldID(env, ndkErrorInfoClass, "index", "I");
	jfieldID fid4 = (*env)->GetFieldID(env, ndkErrorInfoClass, "line", "I");
	if (fid == 0 || fid2 == 0 || fid3 == 0 || fid4 == 0)
		return NULL;
	jobject obj = (*env)->AllocObject(env, ndkErrorInfoClass);

	int errorNum = qspErrorNum;
	char *errorLoc = (qspErrorLoc >= 0 && qspErrorLoc < qspLocsCount ? qspLocs[qspErrorLoc].Name : 0);
	int errorActIndex = qspErrorActIndex;
	int errorLine = qspErrorLine;

	char *sz = qspW2C(errorLoc);
	jstring jLocName = (*env)->NewStringUTF(env, sz);
	if (sz != NULL)
		free(sz);

	(*env)->SetObjectField(env, obj, fid, jLocName);
	(*env)->SetIntField(env, obj, fid2, errorNum);
	(*env)->SetIntField(env, obj, fid3, errorActIndex);
	(*env)->SetIntField(env, obj, fid4, errorLine);
	return obj;
}

/* Get a description of the error by its number */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetErrorDesc(JNIEnv *env, jobject this, jint errorNum)
{
	QSP_CHAR *str;
	switch (errorNum)
	{
		case QSP_ERR_DIVBYZERO: str = QSP_FMT("Division by zero!"); break;
		case QSP_ERR_TYPEMISMATCH: str = QSP_FMT("Type mismatch!"); break;
		case QSP_ERR_STACKOVERFLOW: str = QSP_FMT("Stack overflow!"); break;
		case QSP_ERR_TOOMANYITEMS: str = QSP_FMT("Too many items in expression!"); break;
		case QSP_ERR_FILENOTFOUND: str = QSP_FMT("File not found!"); break;
		case QSP_ERR_CANTLOADFILE: str = QSP_FMT("Can't load file!"); break;
		case QSP_ERR_GAMENOTLOADED: str = QSP_FMT("Game not loaded!"); break;
		case QSP_ERR_COLONNOTFOUND: str = QSP_FMT("Sign [:] not found!"); break;
		case QSP_ERR_CANTINCFILE: str = QSP_FMT("Can't add file!"); break;
		case QSP_ERR_CANTADDACTION: str = QSP_FMT("Can't add action!"); break;
		case QSP_ERR_EQNOTFOUND: str = QSP_FMT("Sign [=] not found!"); break;
		case QSP_ERR_LOCNOTFOUND: str = QSP_FMT("Location not found!"); break;
		case QSP_ERR_ENDNOTFOUND: str = QSP_FMT("[end] not found!"); break;
		case QSP_ERR_LABELNOTFOUND: str = QSP_FMT("Label not found!"); break;
		case QSP_ERR_NOTCORRECTNAME: str = QSP_FMT("Incorrect variable's name!"); break;
		case QSP_ERR_QUOTNOTFOUND: str = QSP_FMT("Quote not found!"); break;
		case QSP_ERR_BRACKNOTFOUND: str = QSP_FMT("Bracket not found!"); break;
		case QSP_ERR_BRACKSNOTFOUND: str = QSP_FMT("Brackets not found!"); break;
		case QSP_ERR_SYNTAX: str = QSP_FMT("Syntax error!"); break;
		case QSP_ERR_UNKNOWNACTION: str = QSP_FMT("Unknown action!"); break;
		case QSP_ERR_ARGSCOUNT: str = QSP_FMT("Incorrect arguments' count!"); break;
		case QSP_ERR_CANTADDOBJECT: str = QSP_FMT("Can't add object!"); break;
		case QSP_ERR_CANTADDMENUITEM: str = QSP_FMT("Can't add menu's item!"); break;
		case QSP_ERR_TOOMANYVARS: str = QSP_FMT("Too many variables!"); break;
		case QSP_ERR_INCORRECTREGEXP: str = QSP_FMT("Regular expression's error!"); break;
		case QSP_ERR_CODENOTFOUND: str = QSP_FMT("Code not found!"); break;
		default: str = QSP_FMT("Unknown error!"); break;
	}

	char *sz = qspW2C(str);
	jstring result = (*env)->NewStringUTF(env, sz);
	if (sz != NULL)
		free(sz);
	return result;
}
/* ------------------------------------------------------------ */
/* Menu */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSelectMenuItem(JNIEnv *env, jobject this, jint ind)
{
	QSPVariant arg;
	if (ind >= 0 && ind < qspCurMenuItems)
	{
		if (qspIsDisableCodeExec) return;
		arg.IsStr = QSP_FALSE;
		QSP_NUM(arg) = ind + 1;
		qspExecLocByNameWithArgs(qspCurMenuLocs[ind], &arg, 1);
	}
}
/* ------------------------------------------------------------ */
/* Game Management */

/* Working with files */

/* Downloading a new game from a file */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPLoadGameWorld(JNIEnv *env, jobject this, jstring fileName)
{
	const char *str = (*env)->GetStringUTFChars(env, fileName, NULL);
	if (str == NULL)
		return JNI_FALSE;

	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspResetError();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	qspOpenQuest((QSP_CHAR *)str, QSP_FALSE);
	if (qspErrorNum) return QSP_FALSE;

	(*env)->ReleaseStringUTFChars(env, fileName, str);
	return QSP_TRUE;
}

/* Saving the state to a file */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSaveGame(JNIEnv *env, jobject this, jstring fileName, jboolean isRefresh)
{
	const char *str = (*env)->GetStringUTFChars(env, fileName, NULL);
	if (str == NULL)
		return JNI_FALSE;

	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspPrepareExecution();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	qspSaveGameStatus((QSP_CHAR*)str);
	if (qspErrorNum) return QSP_FALSE;
	if ((QSP_BOOL)isRefresh) qspCallRefreshInt(QSP_FALSE);

	(*env)->ReleaseStringUTFChars(env, fileName, str);
	return QSP_TRUE;
}

/* Loading status from a file */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPOpenSavedGame(JNIEnv *env, jobject this, jstring fileName, jboolean isRefresh)
{
	const char *str = (*env)->GetStringUTFChars(env, fileName, NULL);
	if (str == NULL)
		return JNI_FALSE;

	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspPrepareExecution();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	qspOpenGameStatus((QSP_CHAR*)str);
	if (qspErrorNum) return QSP_FALSE;
	if ((QSP_BOOL)isRefresh) qspCallRefreshInt(QSP_FALSE);

	(*env)->ReleaseStringUTFChars(env, fileName, str);
	return QSP_TRUE;
}

/* Working with memory */
//
/* Loading a new game from memory */
QSP_BOOL QSPLoadGameWorldFromData(const char *data, int dataSize, const QSP_CHAR *fileName)
{
	char *ptr;
	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspResetError();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	ptr = (char *)malloc(dataSize + 3);
	memcpy(ptr, data, dataSize);
	ptr[dataSize] = ptr[dataSize + 1] = ptr[dataSize + 2] = 0;
	qspOpenQuestFromData(ptr, dataSize + 3, (QSP_CHAR *)fileName, QSP_FALSE);
	free(ptr);
	if (qspErrorNum) return QSP_FALSE;
	return QSP_TRUE;
}
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPLoadGameWorldFromData(JNIEnv *env, jobject this, jbyteArray data, jint dataSize, jstring fileName)
{
	//converting data
	jbyte *jbuf = malloc(dataSize * sizeof(jbyte));
	if (jbuf == NULL)
		return JNI_FALSE;

	(*env)->GetByteArrayRegion(env, data, 0, dataSize, jbuf);
	int size = dataSize;
	char *mydata = (char *)jbuf;

	/* assume the prompt string and user input has less than 128
		characters */
	int fileNameLen = (*env)->GetStringLength(env, fileName) + 1;
	char buf[fileNameLen];
	const jbyte *str;
	str = (*env)->GetStringUTFChars(env, fileName, NULL);
	if (str == NULL) {
		free(jbuf);
		return JNI_FALSE; /* OutOfMemoryError already thrown */
	}

	QSP_CHAR *wcs = qspC2W(str);
	jboolean result = QSPLoadGameWorldFromData(mydata, size, wcs);
	(*env)->ReleaseStringUTFChars(env, fileName, str);
	free(wcs);

	free(jbuf);
	return result;
}

/* Saving state to memory */
QSP_BOOL QSPSaveGameAsData(void **buf, int *realSize, QSP_BOOL isRefresh)
{
	int len, size;
	QSP_CHAR *data;
	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspPrepareExecution();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	if (!(len = qspSaveGameStatusToString(&data)))
	{
		*realSize = 0;
		return QSP_FALSE;
	}
	size = len * sizeof(QSP_CHAR);
	*realSize = size;

	*buf = malloc(size);
	if (*buf == NULL)
	{
		free(data);
		return QSP_FALSE;
	}

	memcpy(*buf, data, size);
	free(data);
	if (isRefresh) qspCallRefreshInt(QSP_FALSE);
	return QSP_TRUE;
}
JNIEXPORT jbyteArray JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSaveGameAsData(JNIEnv *env, jobject this, jboolean isRefresh)
{
	void *buffer = NULL;
	int bufferSize = 0;
	if (QSPSaveGameAsData(&buffer, &bufferSize, (QSP_BOOL)isRefresh) == QSP_FALSE)
		return NULL;

	jbyteArray result;
	result = (*env)->NewByteArray(env, bufferSize);
	if (result == NULL)
		return NULL;

	(*env)->SetByteArrayRegion(env, result, 0, bufferSize, buffer);

	return result;
}

/* Loading state from memory */
QSP_BOOL QSPOpenSavedGameFromData(const void *data, int dataSize, QSP_BOOL isRefresh)
{
	int dataLen;
	QSP_CHAR *ptr;
	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspPrepareExecution();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	dataLen = dataSize / sizeof(QSP_CHAR);
	ptr = (QSP_CHAR *)malloc((dataLen + 1) * sizeof(QSP_CHAR));
	memcpy(ptr, data, dataSize);
	ptr[dataLen] = 0;
	qspOpenGameStatusFromString(ptr);
	free(ptr);
	if (qspErrorNum) return QSP_FALSE;
	if (isRefresh) qspCallRefreshInt(QSP_FALSE);
	return QSP_TRUE;
}
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPOpenSavedGameFromData(JNIEnv *env, jobject this, jbyteArray data, jint dataSize, jboolean isRefresh)
{
	//converting data
	jbyte *jbuf = malloc(dataSize * sizeof(jbyte));
	if (jbuf == NULL)
		return JNI_FALSE;

	(*env)->GetByteArrayRegion(env, data, 0, dataSize, jbuf);
	int size = dataSize;
	void *mydata = (void *)jbuf;

	jboolean result = QSPOpenSavedGameFromData(mydata, size, (QSP_BOOL)isRefresh) == QSP_TRUE;

	free(jbuf);
	return result;
}

/* Loading the state from memory via a string */
QSP_BOOL QSPOpenSavedGameFromString(const QSP_CHAR* str, QSP_BOOL isRefresh)
{
	if (qspIsExitOnError && qspErrorNum) return QSP_FALSE;
	qspPrepareExecution();
	if (qspIsDisableCodeExec) return QSP_FALSE;
	qspOpenGameStatusFromString((QSP_CHAR*)str);
	if (qspErrorNum) return QSP_FALSE;
	if (isRefresh) qspCallRefreshInt(QSP_FALSE);
	return QSP_TRUE;
}

/* Restarting the game */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPRestartGame(JNIEnv *env, jobject this, jboolean isRefresh)
{
	if (qspIsExitOnError && qspErrorNum) return (jboolean)QSP_FALSE;
	qspPrepareExecution();
	if (qspIsDisableCodeExec) return (jboolean)QSP_FALSE;
	qspNewGame((jboolean)QSP_TRUE);
	if (qspErrorNum) return (jboolean)QSP_FALSE;
	if ((QSP_BOOL)isRefresh) qspCallRefreshInt((jboolean)QSP_FALSE);
	return (jboolean)QSP_TRUE;
}
/* ------------------------------------------------------------ */
/* Installing CALLBACKS */
//void QSPSetCallBack(int type, QSP_CALLBACK func)
//{
//	qspSetCallBack(type, func);
//}
/* ------------------------------------------------------------ */
/* Initialization */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPInit(JNIEnv *env, jobject this)
{
	#ifdef _DEBUG
		mwInit();
	#endif
	qspIsDebug = QSP_FALSE;
	qspRefreshCount = qspFullRefreshCount = 0;
	qspQstPath = qspQstFullPath = 0;
	qspQstPathLen = 0;
	qspQstCRC = 0;
	qspRealCurLoc = -1;
	qspRealActIndex = -1;
	qspRealLine = 0;
	qspMSCount = 0;
	qspLocs = 0;
	qspLocsNames = 0;
	qspLocsCount = 0;
	qspCurLoc = -1;
	qspTimerInterval = 0;
	qspCurIsShowObjs = qspCurIsShowActs = qspCurIsShowVars = qspCurIsShowInput = QSP_TRUE;
	setlocale(LC_ALL, QSP_LOCALE);
	qspSetSeed(0);
	qspPrepareExecution();
	qspMemClear(QSP_TRUE);
	qspInitCallBacks();
	qspInitStats();
	qspInitMath();

	jclass clazz;

	/* Get JVM references */
	(*env)->GetJavaVM(env, &ndkJvm);

	clazz = (*env)->FindClass(env, "org/qp/android/questopiabundle/libbravo/NDKLib");
	ndkApiClass = (jclass)(*env)->NewGlobalRef(env, clazz);
	ndkApiObject = (jobject)(*env)->NewGlobalRef(env, this);

	clazz = (*env)->FindClass(env, "org/qp/android/questopiabundle/libbravo/NDKLib$LibListItem");
	ndkListItemClass = (jclass)(*env)->NewGlobalRef(env, clazz);

	clazz = (*env)->FindClass(env, "com/libqsp/jni/QSPLib$ExecutionState");
	ndkExecutionStateClass = (jclass)(*env)->NewGlobalRef(env, clazz);

	clazz = (*env)->FindClass(env, "org/qp/android/questopiabundle/libbravo/NDKLib$LibErrorData");
	ndkErrorInfoClass = (jclass)(*env)->NewGlobalRef(env, clazz);

	clazz = (*env)->FindClass(env, "org/qp/android/questopiabundle/libbravo/NDKLib$LibVarValResp");
	ndkVarValResp = (jclass)(*env)->NewGlobalRef(env, clazz);

	/* Get references to callbacks */
	qspSetCallBack(QSP_CALL_DEBUG, (*env)->GetMethodID(env, ndkApiClass, "CallDebug", "(Ljava/lang/String;)V"));
	qspSetCallBack(QSP_CALL_ISPLAYINGFILE, (*env)->GetMethodID(env, ndkApiClass, "IsPlayingFile", "(Ljava/lang/String;)Z"));
	qspSetCallBack(QSP_CALL_PLAYFILE, (*env)->GetMethodID(env, ndkApiClass, "PlayFile", "(Ljava/lang/String;I)V"));
	qspSetCallBack(QSP_CALL_CLOSEFILE, (*env)->GetMethodID(env, ndkApiClass, "CloseFile", "(Ljava/lang/String;)V"));
	qspSetCallBack(QSP_CALL_SHOWIMAGE, (*env)->GetMethodID(env, ndkApiClass, "ShowPicture", "(Ljava/lang/String;)V"));
	qspSetCallBack(QSP_CALL_SHOWWINDOW, (*env)->GetMethodID(env, ndkApiClass, "ShowWindow", "(IZ)V"));
	qspSetCallBack(QSP_CALL_SHOWMENU, (*env)->GetMethodID(env, ndkApiClass, "ShowMenu", "()V"));
	qspSetCallBack(QSP_CALL_SHOWMSGSTR, (*env)->GetMethodID(env, ndkApiClass, "ShowMessage", "(Ljava/lang/String;)V"));
	qspSetCallBack(QSP_CALL_REFRESHINT, (*env)->GetMethodID(env, ndkApiClass, "RefreshInt", "()V"));
	qspSetCallBack(QSP_CALL_SETTIMER, (*env)->GetMethodID(env, ndkApiClass, "SetTimer", "(I)V"));
//	qspSetCallBack(QSP_CALL_SETINPUTSTRTEXT, (*env)->GetMethodID(env, ndkApiClass, "", "(Ljava/lang/String;)V"));
//	qspSetCallBack(QSP_CALL_SYSTEM, (*env)->GetMethodID(env, ndkApiClass, "onSystem", "(Ljava/lang/String;)V"));
	qspSetCallBack(QSP_CALL_DELETEMENU, (*env)->GetMethodID(env, ndkApiClass, "DeleteMenu", "()V"));
	qspSetCallBack(QSP_CALL_OPENGAMESTATUS, (*env)->GetMethodID(env, ndkApiClass, "OpenGame", "(Ljava/lang/String;)V"));
	qspSetCallBack(QSP_CALL_SAVEGAMESTATUS, (*env)->GetMethodID(env, ndkApiClass, "SaveGame", "(Ljava/lang/String;)V"));
	qspSetCallBack(QSP_CALL_SLEEP, (*env)->GetMethodID(env, ndkApiClass, "Wait", "(I)V"));
	qspSetCallBack(QSP_CALL_GETMSCOUNT, (*env)->GetMethodID(env, ndkApiClass, "GetMSCount", "()I"));
	qspSetCallBack(QSP_CALL_INPUTBOX, (*env)->GetMethodID(env, ndkApiClass, "InputBox", "(Ljava/lang/String;)Ljava/lang/String;"));
	qspSetCallBack(QSP_CALL_ADDMENUITEM, (*env)->GetMethodID(env, ndkApiClass, "AddMenuItem", "(Ljava/lang/String;Ljava/lang/String;)V"));
	qspSetCallBack(QSP_CALL_GETFILECONTENT, (*env)->GetMethodID(env, ndkApiClass, "GetFileContents", "(Ljava/lang/String;)[B"));
	qspSetCallBack(QSP_CALL_CHANGEQUESTPATH, (*env)->GetMethodID(env, ndkApiClass, "ChangeQuestPath", "(Ljava/lang/String;)V"));
}

/* Deinitialization */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPDeInit(JNIEnv *env, jobject this)
{
	qspMemClear(QSP_FALSE);
	qspCreateWorld(0, 0);
	if (qspQstPath) free(qspQstPath);
	if (qspQstFullPath) free(qspQstFullPath);
	#ifdef _DEBUG
		mwTerm();
	#endif

	/* Release references */
	(*env)->DeleteGlobalRef(env, ndkApiObject);
	(*env)->DeleteGlobalRef(env, ndkApiClass);
	(*env)->DeleteGlobalRef(env, ndkListItemClass);
	(*env)->DeleteGlobalRef(env, ndkExecutionStateClass);
	(*env)->DeleteGlobalRef(env, ndkErrorInfoClass);
}

#endif
