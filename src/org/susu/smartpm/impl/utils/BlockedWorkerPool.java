package org.susu.smartpm.impl.utils;

import org.susu.smartpm.api.process.Manageable;
import org.susu.smartpm.api.utils.WorkerPool;

public class BlockedWorkerPool implements WorkerPool {
	static BlockedWorkerPool instance = new BlockedWorkerPool();

	public static BlockedWorkerPool getInstance() {
		return instance;
	}

	public int getTaskNum() {
		// TODO: Implement this method
		return 0;
	}

	public void schedule(Manageable task) {
		if (task != null) {
			task.run();
		}
	}

}
