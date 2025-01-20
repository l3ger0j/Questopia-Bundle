/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_qp_android_questopiabundle_libbravo_NDKLib */

#ifndef _Included_org_qp_android_questopiabundle_libbravo_NDKLib
#define _Included_org_qp_android_questopiabundle_libbravo_NDKLib
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPInit
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPDeInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPDeInit
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPIsInCallBack
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsInCallBack
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPEnableDebugMode
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPEnableDebugMode
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetCurStateData
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetCurStateData
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVersion
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetCompiledDateTime
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetCompiledDateTime
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetFullRefreshCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetFullRefreshCount
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetQstFullPath
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetQstFullPath
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetCurLoc
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetCurLoc
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetExprValue
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetExprValue
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetMainDesc
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetMainDesc
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPIsMainDescChanged
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsMainDescChanged
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetVarsDesc
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVarsDesc
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPIsVarsDescChanged
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsVarsDescChanged
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetVarValuesCount
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVarValuesCount
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetVarValues
 * Signature: (Ljava/lang/String;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVarValues
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetMaxVarsCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetMaxVarsCount
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetVarNameByIndex
 * Signature: (I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetVarNameByIndex
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPSetInputStrText
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSetInputStrText
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetActionsCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetActionsCount
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetActionData
 * Signature: (I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetActionData
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPExecuteSelActionCode
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecuteSelActionCode
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPSetSelActionIndex
 * Signature: (IZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSetSelActionIndex
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetSelActionIndex
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetSelActionIndex
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPIsActionsChanged
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsActionsChanged
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetObjectData
 * Signature: (I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetObjectData
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetObjectsCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetObjectsCount
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPSetSelObjectIndex
 * Signature: (IZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSetSelObjectIndex
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetSelObjectIndex
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetSelObjectIndex
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPIsObjectsChanged
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPIsObjectsChanged
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPExecString
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecString
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPExecLocationCode
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecLocationCode
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPExecCounter
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecCounter
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPExecUserInput
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPExecUserInput
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetLastErrorData
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetLastErrorData
  (JNIEnv *, jobject);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPGetErrorDesc
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPGetErrorDesc
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPLoadGameWorld
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPLoadGameWorld
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPLoadGameWorldFromData
 * Signature: ([BILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPLoadGameWorldFromData
  (JNIEnv *, jobject, jbyteArray, jint, jstring);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPSaveGame
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSaveGame
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPSaveGameAsData
 * Signature: (Z)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSaveGameAsData
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPOpenSavedGame
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPOpenSavedGame
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPOpenSavedGameFromData
 * Signature: ([BIZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPOpenSavedGameFromData
  (JNIEnv *, jobject, jbyteArray, jint, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPRestartGame
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPRestartGame
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     org_qp_android_questopiabundle_libbravo_NDKLib
 * Method:    QSPSelectMenuItem
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_qp_android_questopiabundle_libbravo_NDKLib_QSPSelectMenuItem
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
