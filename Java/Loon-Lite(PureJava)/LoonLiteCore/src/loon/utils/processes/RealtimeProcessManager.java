/**
 * 
 * Copyright 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.4.1
 */
package loon.utils.processes;

import java.util.Comparator;
import java.util.Iterator;

import loon.LRelease;
import loon.LSystem;
import loon.utils.IArray;
import loon.utils.LIterator;
import loon.utils.SortedList;
import loon.utils.TArray;
import loon.utils.timer.LTimerContext;

public class RealtimeProcessManager implements RealtimeProcessEvent, IArray, LRelease {

	static class ProcessComparator implements Comparator<GameProcess> {

		@Override
		public int compare(GameProcess o1, GameProcess o2) {
			if (o1 == null || o2 == null) {
				return 0;
			}
			return o2.getPriority() - o1.getPriority();
		}

	}

	private static final ProcessComparator _processComparator = new ProcessComparator();

	private final TArray<GameProcess> deadProcesses = new TArray<GameProcess>();

	private final TArray<GameProcess> toBeUpdated = new TArray<GameProcess>();
	
	private static RealtimeProcessManager instance;

	private SortedList<GameProcess> processes;

	public static void freeStatic() {
		instance = null;
	}

	public static final RealtimeProcessManager get() {
		if (instance == null) {
			synchronized (RealtimeProcessManager.class) {
				if (instance == null) {
					instance = new RealtimeProcessManager();
				}
			}
		}
		return instance;
	}

	private RealtimeProcessManager() {
		this.processes = new SortedList<GameProcess>();
	}

	public static RealtimeProcessManager newProcess() {
		return new RealtimeProcessManager();
	}

	@Override
	public void addProcess(GameProcess realtimeProcess) {
		synchronized (this.processes) {
			this.processes.add(realtimeProcess);
		}
	}

	@Override
	public boolean containsProcess(GameProcess realtimeProcess) {
		synchronized (this.processes) {
			return this.processes.contains(realtimeProcess);
		}
	}

	@Override
	public void tick(LTimerContext time) {
		if (processes.size > 0) {
			synchronized (this.processes) {
				toBeUpdated.clear();
				toBeUpdated.addAll(this.processes);
			}
			deadProcesses.clear();
			try {
				for (Iterator<GameProcess> it = toBeUpdated.iterator(); it.hasNext();) {
					GameProcess realtimeProcess = it.next();
					if (realtimeProcess != null) {
						synchronized (realtimeProcess) {
							realtimeProcess.tick(time);
							if (realtimeProcess.isDead()) {
								deadProcesses.add(realtimeProcess);
							}
						}
					}
				}
				if (deadProcesses.size > 0) {
					for (Iterator<GameProcess> it = deadProcesses.iterator(); it.hasNext();) {
						GameProcess realtimeProcess = it.next();
						if (realtimeProcess != null) {
							synchronized (realtimeProcess) {
								realtimeProcess.finish();
							}
						}
					}
					synchronized (this.processes) {
						this.processes.removeAll(deadProcesses);
					}
				}
			} catch (Throwable cause) {
				LSystem.error("Process dispatch failure", cause);
			}
		}
	}

	public TArray<GameProcess> find(String id) {
		TArray<GameProcess> list = new TArray<GameProcess>();
		if (processes != null && processes.size > 0) {
			synchronized (this.processes) {
				for (LIterator<GameProcess> it = processes.listIterator(); it.hasNext();) {
					GameProcess p = it.next();
					if (p != null && (p.getId() == id || p.getId().equals(id))) {
						list.add(p);
					}
				}
			}
		}
		return list;
	}

	public TArray<GameProcess> find(GameProcessType pt) {
		TArray<GameProcess> list = new TArray<GameProcess>();
		if (processes != null && processes.size > 0) {
			synchronized (this.processes) {
				for (LIterator<GameProcess> it = processes.listIterator(); it.hasNext();) {
					GameProcess p = it.next();
					if (p != null && p.getProcessType() == pt) {
						list.add(p);
					}
				}
			}
		}
		return list;
	}

	public TArray<GameProcess> delete(GameProcessType pt) {
		TArray<GameProcess> list = new TArray<GameProcess>();
		if (pt == null) {
			return list;
		}
		if (processes != null && processes.size > 0) {
			synchronized (this.processes) {
				final TArray<GameProcess> ps = new TArray<GameProcess>(processes);
				for (int i = 0; i < ps.size; i++) {
					GameProcess p = ps.get(i);
					if (p != null) {
						if (p.getProcessType() == pt) {
							p.kill();
							processes.remove(p);
							list.add(p);
						}
					}
				}
			}
		}
		return list;
	}

	public TArray<GameProcess> delete(GameProcess process) {
		TArray<GameProcess> list = new TArray<GameProcess>();
		if (process == null) {
			return list;
		}
		if (processes != null && processes.size > 0) {
			synchronized (this.processes) {
				final TArray<GameProcess> ps = new TArray<GameProcess>(processes);
				for (int i = 0; i < ps.size; i++) {
					GameProcess p = ps.get(i);
					if (p != null) {
						if (process == p || process.getId() == p.getId() || process.getId().equals(p.getId())) {
							p.kill();
							processes.remove(p);
							list.add(p);
						}
					}
				}
			}
		}
		return list;
	}

	public TArray<GameProcess> delete(String id) {
		TArray<GameProcess> list = new TArray<GameProcess>();
		if (processes != null && processes.size > 0) {
			synchronized (this.processes) {
				final TArray<GameProcess> ps = new TArray<GameProcess>(processes);
				for (int i = 0; i < ps.size; i++) {
					GameProcess p = ps.get(i);
					if (p != null) {
						if (p.getId() == id || p.getId().equals(id)) {
							p.kill();
							processes.remove(p);
							list.add(p);
						}
					}
				}
			}
		}
		return list;
	}

	public TArray<GameProcess> deleteIndex(String id) {
		TArray<GameProcess> list = new TArray<GameProcess>();
		if (processes != null && processes.size > 0) {
			synchronized (this.processes) {
				final TArray<GameProcess> ps = new TArray<GameProcess>(processes);
				for (int i = 0; i < ps.size; i++) {
					GameProcess p = ps.get(i);
					if (p != null) {
						if (p.getId() == id || p.getId().indexOf(id) != -1) {
							p.kill();
							processes.remove(p);
							list.add(p);
						}
					}
				}
			}
		}
		return list;
	}

	public RealtimeProcessManager sort() {
		synchronized (this.processes) {
			processes.sort(_processComparator);
		}
		return this;
	}

	public boolean hasEvents() {
		return !isEmpty();
	}

	@Override
	public int size() {
		return processes.size;
	}

	@Override
	public void clear() {
		processes.clear();
	}

	@Override
	public boolean isEmpty() {
		return processes.size == 0;
	}

	public void dispose() {
		close();
	}

	@Override
	public void close() {
		if (processes != null && processes.size > 0) {
			synchronized (this.processes) {
				final TArray<GameProcess> ps = new TArray<GameProcess>(processes);
				for (int i = 0; i < ps.size; i++) {
					GameProcess p = ps.get(i);
					if (p != null) {
						synchronized (p) {
							p.finish();
						}
					}
				}
				processes.clear();
			}
		}
	}

}