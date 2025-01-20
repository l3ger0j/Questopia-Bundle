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

#include <jni.h>
#include <uchar.h>

#ifndef QSP_ANDROIDDEFINES
	#define QSP_ANDROIDDEFINES

	#ifdef _UNICODE
		typedef char16_t QSP_CHAR;
	#endif

	typedef jmethodID QSP_CALLBACK;

	#include "../qsp.h"
	#include "org_qp_android_questopiabundle_libbravo_NDKLib.h"

	extern JavaVM *ndkJvm;
	extern jclass ndkApiClass;
	extern jobject ndkApiObject;

	extern jclass ndkListItemClass;
	extern jclass ndkExecutionStateClass;
	extern jclass ndkErrorInfoClass;
	extern jclass ndkVarValResp;

	char *qspW2C(QSP_CHAR *);
	QSP_CHAR *qspC2W(char *);
	jstring qspToJVMString(JNIEnv *, QSP_CHAR *);

#endif
