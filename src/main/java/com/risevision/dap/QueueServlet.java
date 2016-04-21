package com.risevision.dap;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class QueueServlet extends HttpServlet {

	private static final long serialVersionUID = 8552867604600145694L;

	private Logger log = Logger.getLogger(QueueServlet.class.getSimpleName());

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)	throws IOException {

		String task = req.getParameter(QueryParam.TASK);

		if (task == null || task.isEmpty()) {
			log.severe("Task is not supplied, exiting.");
			return;
		}

		log.info(String.format("task: '%s'", task));

		try {

			if (task.equals(QueueTask.ENQUEUE.getName())) {

				String taskName = req.getParameter(QueryParam.TASK_NAME);

				QueueFactory.getQueue(QueueName.DEFAULT.getName()).add(withUrl("/queue")
						.param(QueryParam.TASK, taskName)
						.method(Method.GET));

			} else if (task.equals(QueueTask.PRERENDER.getName())) {

				new PrerenderDisplayActivation().execute(req, resp);	

			} else if (task.equals(QueueTask.CHECK_STATUS.getName())) {

				new CheckStatus().execute(req, resp);	

			} else {

				log.warning("Task " + task + " is not recognized, exiting.");
				return;
			}

		} catch (Exception e) {

			Utils.logException(e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}

}
