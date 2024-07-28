package dev.arichard.parqueditor.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

@Service
public class ThreadService {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public <T> void executeTaskThenUpdateUi(Callable<T> action, Consumer<T> onSucceeded,
            Consumer<WorkerStateEvent> onFailed) {
        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return action.call();
            }
        };
        if (onSucceeded != null) {
            task.setOnSucceeded(e -> onSucceeded.accept(task.getValue()));
        }
        if (onFailed != null) {
            task.setOnFailed(e -> onFailed.accept(e));
        }
        executorService.execute(task);
    }

}
