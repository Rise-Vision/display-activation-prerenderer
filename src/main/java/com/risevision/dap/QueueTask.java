package com.risevision.dap;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.risevision.dap.QueryParam;

public enum QueueTask {

	ENQUEUE("enqueue", QueueName.DEFAULT, TaskOptions.Method.GET),        
	PRERENDER("prerender", QueueName.DEFAULT, TaskOptions.Method.GET),
	CHECK_STATUS("check_status", QueueName.DEFAULT, TaskOptions.Method.GET);

	private final String name;
	private final TaskOptions.Method method;
	private final QueueName queueName;

	QueueTask(final String name, final QueueName queueName, final TaskOptions.Method method) {

		this.name = name;
		this.method = method;
		this.queueName = queueName;
	}

	public String getName() {
		return name;
	}

	public QueueName getQueueName() {
		return queueName;
	}

	public TaskOptions.Method getMethod() {
		return method;
	}

	public void enqueue(final Map<String, String> params) {

		this.enqueue(params, null);
	}

	public void enqueue(final Map<String, String> params, long deferMs) {

		this.enqueue(params, null, deferMs);
	}

	public void enqueue(final Map<String, String> params, final Transaction txn) {

		this.enqueue(params, txn, 0);
	}

	public void enqueue(final Map<String, String> params, final Transaction txn, long deferMs) {

		Logger.getAnonymousLogger().info(String.format("enqueuing task %s", this.getName()));

		TaskOptions options = withUrl("/queue")
				.param(QueryParam.TASK, getName())
				.method(this.getMethod());

		if (params != null) {

			for (Map.Entry<String, String> entry : params.entrySet()) {

				if (entry.getValue() != null) {

					options = options.param(entry.getKey(), entry.getValue());
				}
			}
		}

		if (deferMs > 0) {

			options = options.countdownMillis(deferMs);
		}

		String qName = this.getQueueName() == null ? QueueName.DEFAULT.getName() : this.getQueueName().getName(); 

		addToQueue(qName, options, txn);

	}

	private void addToQueue(String queueName, TaskOptions taskOptions, Transaction txn) {

		Queue q = QueueFactory.getQueue(queueName);

		if (txn != null) {
			q.add(txn, taskOptions);
		}
		else {
			q.add(taskOptions);
		}
	}

}
