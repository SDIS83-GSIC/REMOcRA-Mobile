package fr.sdis83.remocra.mobile.utils

import android.content.Context

abstract class SingletonHolder<T> {
    @Volatile
    private var instance: T? = null

    // double-checked locking
    fun getInstance(context: Context) =
        instance ?: synchronized(this) {
            instance ?: newInstance(context).also {
                instance = it
            }
        }

    fun clearInstance() {
        instance = null
    }

    protected abstract fun newInstance(context: Context): T
}
