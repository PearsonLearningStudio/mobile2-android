package com.ecollege.android.tasks;

public interface SmartAsyncTaskCallable<ParameterT, ProgressT, ReturnT> {

    public ReturnT call(SmartAsyncTask<ParameterT, ProgressT, ReturnT> task) throws Exception;

}
