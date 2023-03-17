/**
 * Copyright 2008 - 2010
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
 * @email javachenpeng@yahoo.com
 * @version 0.1.1
 */
package loon.action.map;

import loon.LRelease;
import loon.LSystem;
import loon.action.map.heuristics.BestFirst;
import loon.action.map.heuristics.Closest;
import loon.action.map.heuristics.ClosestSquared;
import loon.action.map.heuristics.Diagonal;
import loon.action.map.heuristics.DiagonalMin;
import loon.action.map.heuristics.DiagonalShort;
import loon.action.map.heuristics.Euclidean;
import loon.action.map.heuristics.EuclideanNoSQR;
import loon.action.map.heuristics.Manhattan;
import loon.action.map.heuristics.Mixing;
import loon.action.map.heuristics.Octile;
import loon.events.Updateable;
import loon.geom.Vector2f;
import loon.utils.IntMap;
import loon.utils.ObjectSet;
import loon.utils.TArray;

/**
 * A*寻径用类
 */
public class AStarFinder implements Updateable, LRelease {

	public static final int DIJKSTRA = 0;

	public static final int ASTAR = 1;

	private class ScoredPath {

		private float score;

		private TArray<Vector2f> pathList;

		private ScoredPath(float score, TArray<Vector2f> pathList) {
			this.score = score;
			this.pathList = pathList;
		}

	}

	public final static AStarFindHeuristic ASTAR_CLOSEST = new Closest();

	public final static AStarFindHeuristic ASTAR_CLOSEST_SQUARED = new ClosestSquared();

	public final static AStarFindHeuristic ASTAR_MANHATTAN = new Manhattan();

	public final static AStarFindHeuristic ASTAR_DIAGONAL = new Diagonal();

	public final static AStarFindHeuristic ASTAR_EUCLIDEAN = new Euclidean();

	public final static AStarFindHeuristic ASTAR_EUCLIDEAN_NOSQR = new EuclideanNoSQR();

	public final static AStarFindHeuristic ASTAR_MIXING = new Mixing();

	public final static AStarFindHeuristic ASTAR_DIAGONAL_SHORT = new DiagonalShort();

	public final static AStarFindHeuristic ASTAR_BEST_FIRST = new BestFirst();

	public final static AStarFindHeuristic ASTAR_OCTILE = new Octile();

	public final static AStarFindHeuristic ASTAR_DIAGONAL_MIN = new DiagonalMin();

	private final static IntMap<TArray<Vector2f>> FINDER_LAZY = new IntMap<>(100);

	private final static int makeLazyKey(AStarFindHeuristic heuristic, int[][] map, int[] limits, int sx, int sy,
			int ex, int ey, boolean flag) {
		int hashCode = 1;
		int w = map.length;
		int h = map[0].length;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				hashCode = LSystem.unite(hashCode, map[i][j]);
			}
		}
		if (limits != null) {
			for (int limit : limits) {
				hashCode = LSystem.unite(hashCode, limit);
			}
		}
		hashCode = LSystem.unite(hashCode, heuristic.getType());
		hashCode = LSystem.unite(hashCode, sx);
		hashCode = LSystem.unite(hashCode, sy);
		hashCode = LSystem.unite(hashCode, ex);
		hashCode = LSystem.unite(hashCode, ey);
		hashCode = LSystem.unite(hashCode, flag);
		return hashCode;
	}

	public static TArray<Vector2f> find(AStarFindHeuristic heuristic, int[][] maps, int[] limits, int x1, int y1,
			int x2, int y2, boolean flag) {
		heuristic = (heuristic == null ? ASTAR_MANHATTAN : heuristic);
		synchronized (FINDER_LAZY) {
			if (FINDER_LAZY.size >= LSystem.DEFAULT_MAX_CACHE_SIZE * 10) {
				FINDER_LAZY.clear();
			}
			int key = makeLazyKey(heuristic, maps, limits, x1, y1, x2, y2, flag);
			TArray<Vector2f> result = FINDER_LAZY.get(key);
			if (result == null) {
				AStarFinder astar = new AStarFinder(heuristic, ASTAR);
				Field2D fieldMap = new Field2D(maps);
				if (limits != null) {
					fieldMap.setLimit(limits);
				}
				Vector2f start = new Vector2f(x1, y1);
				Vector2f over = new Vector2f(x2, y2);
				result = astar.calc(fieldMap, start, over, flag);
				FINDER_LAZY.put(key, result);
				astar.close();
			}
			if (result != null) {
				TArray<Vector2f> newResult = new TArray<>();
				newResult.addAll(result);
				result = newResult;
			}
			if (result == null) {
				return new TArray<>();
			}
			return new TArray<>(result);
		}
	}

	public static TArray<Vector2f> find(int[][] maps, int x1, int y1, int x2, int y2, boolean flag) {
		return find(null, maps, x1, y1, x2, y2, flag);
	}

	public static TArray<Vector2f> find(AStarFindHeuristic heuristic, int[][] maps, int x1, int y1, int x2, int y2,
			boolean flag) {
		return find(heuristic, maps, x1, y1, x2, y2, flag);
	}

	public static TArray<Vector2f> find(HexagonMap map, int x1, int y1, int x2, int y2, boolean flag) {
		return find(null, map.getField2D().getMap(), map.getLimit(), x1, y1, x2, y2, flag);
	}

	public static TArray<Vector2f> find(TileMap map, int x1, int y1, int x2, int y2, boolean flag) {
		return find(null, map.getField2D().getMap(), map.getLimit(), x1, y1, x2, y2, flag);
	}

	public static TArray<Vector2f> find(Field2D maps, int x1, int y1, int x2, int y2, boolean flag) {
		return find(null, maps.getMap(), maps.getLimit(), x1, y1, x2, y2, flag);
	}

	public static TArray<Vector2f> find(AStarFindHeuristic heuristic, Field2D maps, int x1, int y1, int x2, int y2,
			boolean flag) {
		return find(heuristic, maps.getMap(), maps.getLimit(), x1, y1, x2, y2, flag);
	}

	public static TArray<Vector2f> find(AStarFindHeuristic heuristic, Field2D maps, Vector2f start, Vector2f goal,
			boolean flag) {
		return find(heuristic, maps.getMap(), maps.getLimit(), start.x(), start.y(), goal.x(), goal.y(), flag);
	}

	public static TArray<Vector2f> find(AStarFindHeuristic heuristic, int[][] maps, Vector2f start, Vector2f goal,
			boolean flag) {
		return find(heuristic, maps, start.x(), start.y(), goal.x(), goal.y(), flag);
	}

	private Vector2f goal;

	private TArray<ScoredPath> nextList;

	private TArray<Vector2f> pathList;

	private ObjectSet<Vector2f> openList;

	private ObjectSet<Vector2f> closedList;

	private ScoredPath spath;

	private boolean flying, alldirMove, closed, running;

	private Field2D findMap;

	private int algorithm = ASTAR;

	private int overflow = 4096;

	private int startX, startY, endX, endY;

	private AStarFinderListener pathFoundListener;

	private AStarFindHeuristic findHeuristic;

	public AStarFinder(AStarFindHeuristic heuristic, int algorithm) {
		this(heuristic, false, algorithm);
	}

	public AStarFinder(AStarFindHeuristic heuristic, boolean flying, int algorithm) {
		this(heuristic, (Field2D) null, 0, 0, 0, 0, flying, false, algorithm);
	}

	public AStarFinder(AStarFindHeuristic heuristic, Field2D m, int startX, int startY, int endX, int endY,
			boolean flying, boolean flag, int algorithm) {
		this(heuristic, m, startX, startY, endX, endY, flying, flag, null, algorithm);
	}

	public AStarFinder(AStarFindHeuristic heuristic, Field2D m, int startX, int startY, int endX, int endY,
			boolean flying, boolean flag, AStarFinderListener callback, int algorithm) {
		this.findMap = m;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.flying = flying;
		this.alldirMove = flag;
		this.pathFoundListener = callback;
		this.findHeuristic = heuristic;
		this.algorithm = algorithm;
	}

	public AStarFinder update(AStarFinder find) {
		this.findMap = find.findMap;
		this.startX = find.startX;
		this.startY = find.startY;
		this.endX = find.endX;
		this.endY = find.endY;
		this.flying = find.flying;
		this.alldirMove = find.alldirMove;
		this.findHeuristic = find.findHeuristic;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AStarFinder) {
			return this.pathFoundListener == ((AStarFinder) o).pathFoundListener;
		}
		return false;
	}

	private TArray<Vector2f> calc(Field2D m, Vector2f start, Vector2f goal, boolean flag) {
		if (start.equals(goal)) {
			TArray<Vector2f> v = new TArray<>();
			v.add(start);
			return v;
		}
		this.goal = goal;
		if (openList == null) {
			openList = new ObjectSet<>();
		} else {
			openList.clear();
		}
		if (closedList == null) {
			closedList = new ObjectSet<>();
		} else {
			closedList.clear();
		}
		if (nextList == null) {
			nextList = new TArray<>();
		} else {
			nextList.clear();
		}
		openList.add(start);
		if (pathList == null) {
			pathList = new TArray<>();
		} else {
			pathList.clear();
		}
		pathList.add(start);
		if (spath == null) {
			spath = new ScoredPath(0, pathList);
		} else {
			spath.score = 0;
			spath.pathList = pathList;
		}
		nextList.add(spath);
		return findPath(m, flag, algorithm);
	}

	public AStarFinder setOverflow(int over) {
		this.overflow = over;
		return this;
	}

	public int getOverflow() {
		return this.overflow;
	}

	public AStarFinder stop() {
		this.running = false;
		return this;
	}

	public boolean isRunning() {
		return this.running;
	}

	public TArray<Vector2f> findPath() {
		Vector2f start = new Vector2f(startX, startY);
		Vector2f over = new Vector2f(endX, endY);
		return calc(findMap, start, over, alldirMove);
	}

	public TArray<Vector2f> findPath(Field2D map, boolean flag, int algorithm) {
		running = true;
		for (int j = 0; nextList.size > 0; j++) {
			if (j > overflow) {
				nextList.clear();
				break;
			}
			if (!running) {
				break;
			}
			ScoredPath spath = nextList.removeIndex(0);
			Vector2f current = spath.pathList.get(spath.pathList.size - 1);
			if (algorithm == ASTAR) {
				closedList.add(current);
			}
			if (current.equals(goal)) {
				return new TArray<>(spath.pathList);
			}
			TArray<Vector2f> step = map.neighbors(current, flag);
			final int size = step.size;
			for (int i = 0; i < size; i++) {
				Vector2f next = step.get(i);
				if (!map.isHit(next) && !flying) {
					continue;
				}
				if (algorithm == ASTAR) {
					if (closedList.contains(next) || !openList.add(next)) {
						continue;
					}
				} else {
					openList.add(next);
				}

				TArray<Vector2f> pathList = new TArray<>(spath.pathList);
				pathList.add(next);
				float score = spath.score + findHeuristic.getScore(goal.x, goal.y, next.x, next.y);
				insert(score, pathList);
			}
		}
		return null;
	}

	private void insert(float score, TArray<Vector2f> list) {
		int size = nextList.size;
		for (int i = 0; i < size; i++) {
			ScoredPath spath = nextList.get(i);
			if (spath.score >= score) {
				nextList.add(new ScoredPath(score, list));
				return;
			}
		}
		nextList.add(new ScoredPath(score, list));
	}

	public int getStartX() {
		return startX;
	}

	public int getStartY() {
		return startY;
	}

	public int getEndX() {
		return endX;
	}

	public int getEndY() {
		return endY;
	}

	public boolean isFlying() {
		return flying;
	}

	public boolean isAllDirectionMove() {
		return alldirMove;
	}

	@Override
	public void action(Object o) {
		if (pathFoundListener != null) {
			pathFoundListener.pathFound(findPath());
		}
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close() {
		if (pathList != null) {
			pathList.clear();
			pathList = null;
		}
		if (nextList != null) {
			nextList.clear();
			nextList = null;
		}
		if (openList != null) {
			openList.clear();
			openList = null;
		}
		if (closedList != null) {
			closedList.clear();
			closedList = null;
		}
		spath = null;
		goal = null;
		closed = true;
		running = false;
	}

}
