/**
 * Copyright 2013-2021 Sylvain Cadilhac (NetFishers)
 * 
 * This file is part of Netshot.
 * 
 * Netshot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Netshot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Netshot.  If not, see <http://www.gnu.org/licenses/>.
 */
package onl.netfishers.netshot.work;

import onl.netfishers.netshot.database.Database;
import onl.netfishers.netshot.TaskManager;
import onl.netfishers.netshot.cluster.ClusterManager;

import org.hibernate.Session;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Quartz job which runs a Netshot task.
 */
@DisallowConcurrentExecution
public class MasterJob implements Job {

	/** The Constant NETSHOT_TASK. */
	public static final String NETSHOT_TASK = "Netshot Task";

	/** The logger. */
	final private static Logger logger = LoggerFactory.getLogger(MasterJob.class);

	/**
	 * Instantiates a new task job.
	 */
	public MasterJob() {
	}

	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("Starting master task job.");
		Long id = (Long) context.getJobDetail().getJobDataMap()
				.get(NETSHOT_TASK);
		logger.trace("The task id is {}.", id);
		Task task = null;
		Session session = Database.getSession();
		try {
			Thread.sleep(500);
			session.beginTransaction();
			task = (Task) session.get(Task.class, id);
			if (task == null) {
				logger.error("The retrieved task {} is null.", id);
			}
			TaskManager.assignTaskRunner(task);
			session.update(task);
			session.getTransaction().commit();
			ClusterManager.requestTasksAssignment();
		}
		catch (Exception e) {
			logger.error("Error while retrieving and assigning the task to a runner.", e);
			try {
				session.getTransaction().rollback();
			}
			catch (Exception e1) {

			}
			throw new JobExecutionException("Unable to assign the task to a runner.");
		}
		finally {
			session.close();
		}
	}

}