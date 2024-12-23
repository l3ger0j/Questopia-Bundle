package org.qp.android.questopiabundle;

import org.qp.android.questopiabundle.LibResult;
import org.qp.android.questopiabundle.LibException;

interface AsyncCallbacks {
    void sendLibGameState(in LibResult libResult);
    void sendLibRef(in LibResult libResult);
    void sendChangeCurrGameDir(in Uri gameDirUri);

    String doOnShowInputDialog(String inputText);

    void onError(in LibException libException);
}