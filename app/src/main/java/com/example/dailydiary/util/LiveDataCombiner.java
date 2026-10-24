package com.example.dailydiary.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public final class LiveDataCombiner {

    public interface Combiner2<A, B, R> {
        R combine(A first, B second);
    }

    public interface Combiner3<A, B, C, R> {
        R combine(A first, B second, C third);
    }

    public interface Combiner4<A, B, C, D, R> {
        R combine(A first, B second, C third, D fourth);
    }

    private LiveDataCombiner() {
    }

    public static <A, B, R> LiveData<R> combine(
            LiveData<A> first,
            LiveData<B> second,
            Combiner2<A, B, R> combiner
    ) {
        MediatorLiveData<R> result = new MediatorLiveData<>();
        Runnable update = () -> result.setValue(combiner.combine(first.getValue(), second.getValue()));
        result.addSource(first, ignored -> update.run());
        result.addSource(second, ignored -> update.run());
        return result;
    }

    public static <A, B, C, R> LiveData<R> combine(
            LiveData<A> first,
            LiveData<B> second,
            LiveData<C> third,
            Combiner3<A, B, C, R> combiner
    ) {
        MediatorLiveData<R> result = new MediatorLiveData<>();
        Runnable update = () -> result.setValue(
                combiner.combine(first.getValue(), second.getValue(), third.getValue()));
        result.addSource(first, ignored -> update.run());
        result.addSource(second, ignored -> update.run());
        result.addSource(third, ignored -> update.run());
        return result;
    }

    public static <A, B, C, D, R> LiveData<R> combine(
            LiveData<A> first,
            LiveData<B> second,
            LiveData<C> third,
            LiveData<D> fourth,
            Combiner4<A, B, C, D, R> combiner
    ) {
        MediatorLiveData<R> result = new MediatorLiveData<>();
        Runnable update = () -> result.setValue(combiner.combine(
                first.getValue(), second.getValue(), third.getValue(), fourth.getValue()));
        result.addSource(first, ignored -> update.run());
        result.addSource(second, ignored -> update.run());
        result.addSource(third, ignored -> update.run());
        result.addSource(fourth, ignored -> update.run());
        return result;
    }
}
