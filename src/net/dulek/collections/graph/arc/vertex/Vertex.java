/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or distribute
 * this software, either in source code form or as a compiled binary, for any
 * purpose, commercial or non-commercial, and by any means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors of this
 * software dedicate any and all copyright interest in the software to the
 * public domain. We make this dedication for the benefit of the public at large
 * and to the detriment of our heirs and successors. We intend this dedication
 * to be an overt act of relinquishment in perpetuity of all present and future
 * rights to this software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dulek.collections.graph.arc.vertex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.dulek.collections.HashSet;
import net.dulek.collections.IdentityHashSet;

/**
 * This represents a vertex in a graph. A vertex is a fundamental unit of which
 * a graph exists, and a possible endpoint for arcs to connect to. The vertex
 * may hold a label.
 * @author Ruben Dulek
 * @param <V> The type of data stored in this vertex.
 * @param <A> The type of data stored in the arcs connecting to this vertex.
 * @version 1.0
 */
public abstract class Vertex<V,A> {
	/**
	 * The serial version unique ID for this class, indicating the version of
	 * the serialisation of this vertex. If the vertex is changed in such a way
	 * that the serialisation of the vertex will be different, a new serial
	 * version ID must be generated.
	 */
	//private static final long serialVersionUID = -1206575267577597095L;

	/**
	 * The set of arcs that are coming into this vertex. These are the arcs that
	 * have this vertex in their destination.
	 */
	private IncomingArcSet incomingArcs;

	/**
	 * The label that this vertex holds. If the vertex is supposed to hold
	 * multiple labels, it is recommended that the vertex is extended with extra
	 * fields, rather than to let the type of labels in the vertex be a tuple.
	 */
	private V label;

	/**
	 * The set of arcs that are going out of this vertex. These are the arcs
	 * that have this vertex in their source.
	 */
	private OutgoingArcSet outgoingArcs;

	/**
	 * The graph that this vertex is a member of. This is mostly required for
	 * optimisation in order to know in advance how big the graph is. It is also
	 * required to copy the context of the vertex when the vertex is copied.
	 * <p>If this vertex is not a member of a graph, this should be
	 * {@code null}. The vertex can only be part of one graph at a time.</p>
	 */
	protected Graph<V,A> graph;

	/**
	 * The unique identifier of this vertex. This is used to identify vertices
	 * in {@code String} representations of the graph.
	 */
	protected int uid;

	/**
	 * Creates a new vertex. The vertex will not be placed in the graph or
	 * connected to any arcs without additional commands.
	 */
	protected Vertex() {
		uid = Graph.nextVertexUID++;
	}

	/**
	 * Returns the set of all vertices adjacent to this vertex. A vertex is
	 * adjacent to this vertex if there exists an arc with this vertex in its
	 * source and the other vertex in its destination. Note that this is not a
	 * symmetrical relation if the graph is directed. Vertices are also not
	 * adjacent to themselves unless there exists an arc with the vertex in its
	 * source as well as in its destination.
	 * <p>The resulting set will be a new, separate set of vertices. The graph
	 * will maintain no reference to the resulting set, nor vice-versa, so
	 * modifying the resulting set will have no influence on the graph or
	 * vice-versa.</p>
	 * @return The set of all vertices adjacent to this vertex.
	 */
	public Set<Vertex<V,A>> adjacentVertices() {
		final Set<Vertex<V,A>> result = new IdentityHashSet<>(outgoingArcs.size()); //Estimate 1 vertex per outgoing arc (correct for non-hypergraphs).
		for(final Arc<V,A> arc : outgoingArcs) {
			result.addAll(arc.destinationEndpoints()); //Set automatically removes double vertices.
		}
		return result;
	}

	/**
	 * Returns whether the specified vertex can be reached from this vertex. A
	 * vertex can be reached from this vertex if there exists a path of zero or
	 * more incident arcs from this vertex to the other.
	 * <p>This implementation performs a breadth-first search, starting from the
	 * specified vertex. Rather than setting a flag in the other vertices for
	 * vertices that have already been visited, the visited vertices are kept in
	 * a set.</p>
	 * @param other The vertex of which it must be determined whether it can be
	 * reached from this vertex.
	 * @return {@code true} if the other vertex can be reached from this vertex
	 * or {@code false} otherwise.
	 */
	public boolean canReach(final Vertex<V,A> other) {
		if(this == other) { //Zero-length paths are positives too.
			return true;
		}
		if(graph == null) {
			return false;
		}
		final Queue<Vertex<V,A>> todo = new ArrayDeque<>(graph.numVertices() >> 3); //This queue will hold the front of our BFS. Estimate the maximum width of the front as n/8.
		final Set<Vertex<V,A>> done = new IdentityHashSet<>(graph.numVertices() >> 1); //The set of vertices we don't want to explore again. Estimate the maximum number of explored vertices as n/2.
		todo.add(this);
		while(!todo.isEmpty()) { //Keep traversing the graph until no more new vertices are reachable.
			final Vertex<V,A> vertex = todo.remove();
			for(final Arc<V,A> arc : vertex.outgoingArcs) {
				for(final Vertex<V,A> adjacentVertex : arc.destinationEndpoints()) {
					if(adjacentVertex == other) { //Considered doing this check in the if-statement below, but if the graph is sparse this check won't occur often and adding to a hashset is expensive.
						return true;
					}
					if(done.add(adjacentVertex)) { //We've never seen this vertex yet.
						todo.offer(adjacentVertex);
					}
				}
			}
		}
		return false; //No more vertices to explore. Not reachable then.
	}

	/**
	 * Creates and returns a deep copy of this vertex. The entire graph will be
	 * copied, and the corresponding vertex of the new graph will be returned.
	 * The new vertex will be equal to the original.
	 * @return A copy of this vertex, embedded in a copy of the graph.
	 * @throws CloneNotSupportedException Cloning is supported, but this allows
	 * extensions of this class to not support cloning.
	 */
	@Override
	@SuppressWarnings("unchecked") //Caused by the cast back to Vertex when cloning vertices without graph.
	public Vertex<V,A> clone() throws CloneNotSupportedException {
		if(graph == null) {
			Vertex<V,A> clone = (Vertex<V,A>)super.clone();
			clone.uid = Graph.nextVertexUID++;
			clone.label = label;
			return (Vertex<V,A>)super.clone();
		}
		return graph.clone(this);
	}

	/**
	 * Connects this vertex to another one. This makes sure the specified vertex
	 * will be adjacent to this vertex when the method call returns. If the
	 * vertex is already adjacent, the graph will not be modified. If it is not,
	 * a new arc will be created with this vertex in its source and the
	 * specified vertex in its destination.
	 * <p>The method will return whether the graph was modified by the method
	 * call. It will return {@code true} when an arc was created and
	 * {@code false} when no such arc was created.</p>
	 * @param other The vertex to which this vertex must be connected.
	 * @return {@code true} if the graph was modified by this method call, or
	 * {@code false} if it was not.
	 * @throws IllegalStateException Connecting this vertex to the other would
	 * cause the graph to become invalid.
	 */
	@SuppressWarnings("element-type-mismatch") //Caused by the call to contains(Object) with net.dulek.collections.graph.Vertex.
	public boolean connect(final Vertex<V,A> other) {
		if(graph == null) { //Not in a graph, then I can't be connecting to anything.
			throw new IllegalStateException("This vertex is not part of a graph. It can't connect to any other vertices.");
		}
		//First check whether the specified vertex is already adjacent.
		for(final Arc<V,A> arc : outgoingArcs) {
			if(arc.destinationEndpoints().contains(other)) { //Already adjacent.
				return false;
			}
		}
		//Then, having ensured it is not yet adjacent, create a new arc with this vertex in its source and the other in its destination.
		graph.addArc(Collections.singleton(this),Collections.singleton(other));
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Returns the label stored in this vertex.
	 * @return The label stored in this vertex.
	 */
	public V getLabel() {
		return label;
	}

	/**
	 * Returns a hash code value for the vertex. This method is supported for
	 * the benefit of hashtables, as well as to function as a heuristic for
	 * isomorphism tests.
	 * <p>The hash code fulfils the general contract of
	 * {@link Object#hashCode()}: It will remain the same as long as the graph
	 * structure doesn't change (which is a very weak promise), and if two
	 * vertices are the same according to the {@link equals(Object)} method,
	 * then the hash code of these two vertices will be the same.</p>
	 * <p>This hash code implementation tries to include as much structural
	 * information about the graph and the vertex' position in it as possible,
	 * while still remaining invariate to graph automorphisms. This
	 * implementation takes into account every vertex and arc in the graph,
	 * hashing their labels and their in-degree and out-degree. The in-degree
	 * and out-degree of the vertices and arcs are raised to the {@code k}th
	 * power where {@code k} equals the smallest distance of that element to the
	 * original vertex in number of vertex/arc traversals. The labels of the
	 * vertices are multiplied by {@code 31^k} and the labels of the arcs are
	 * multiplied by {@code 127^k}. The sum of the combined results for all
	 * reachable vertices and arcs in the graph is then completely flipped (all
	 * bits are flipped), and then this procedure is repeated but this time the
	 * arcs are traversed in the other direction, possibly producing different
	 * values for {@code k} and possibly resulting in a different set of
	 * vertices and arcs being reachable. The combined sum is subtracted from
	 * the previous result, and then returned. This procedure ensures that two
	 * corresponding vertices in isomorphic graphs will get the same hash code.
	 * </p>
	 * <p>Note that this method possibly traverses the entire graph, and
	 * therefore has {@code O(n + m)} worst-case complexity. This is quite
	 * complex for a hash code method. Therefore, identity hashes should be used
	 * where applicable.</p>
	 * @return A hash code for this vertex.
	 */
	@Override
	public int hashCode() {
		if(graph == null) { //No graph, just the vertex.
			if(label == null) {
				return -1; //Empty unconnected vertex.
			}
			return 31 * label.hashCode(); //Unconnected vertex, is just the label then.
		}

		//First, perform a BFS in the forward direction.
		int result = 0;
		int layer = 0; //The distance to the starting vertex.
		Set<Vertex<V,A>> todoVertices = new IdentityHashSet<>(1); //Vertices we still have to process in this layer.
		todoVertices.add(this);
		Set<Arc<V,A>> todoArcs; //Arcs we still have to process in this layer.
		final Set<Vertex<V,A>> doneVertices = new IdentityHashSet<>(graph.numVertices()); //Vertices we don't want to process again.
		final Set<Arc<V,A>> doneArcs = new IdentityHashSet<>(graph.numArcs()); //Arcs we don't want to process again.
		while(!todoVertices.isEmpty()) {
			layer++;
			todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
			for(final Vertex<V,A> vertex : todoVertices) {
				result += (vertex.label == null ? 0 : net.dulek.math.Math.power(31,layer) * vertex.label.hashCode()) + net.dulek.math.Math.power(vertex.outgoingArcs.size(),layer) + net.dulek.math.Math.power(vertex.incomingArcs.size(),layer);
				for(final Arc<V,A> arc : vertex.outgoingArcs) { //Add all unexplored arcs to the todo list.
					if(!doneArcs.contains(arc)) {
						todoArcs.add(arc);
					}
				}
				doneVertices.add(vertex); //Don't visit this vertex ever again.
			}
			todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
			for(final Arc<V,A> arc : todoArcs) {
				result += (arc.getLabel() == null ? 0 : net.dulek.math.Math.power(127,layer) * arc.getLabel().hashCode()) + net.dulek.math.Math.power(arc.destinationEndpoints().size(),layer) + net.dulek.math.Math.power(arc.sourceEndpoints().size(),layer);
				for(final Vertex<V,A> vertex : arc.destinationEndpoints()) { //Add all unexplored vertices to the todo list.
					if(!doneVertices.contains(vertex)) {
						todoVertices.add(vertex);
					}
				}
				doneArcs.add(arc); //Don't visit this arc ever again.
			}
		}

		result ^= -1; //Flip all bits.

		//Next, perform a BFS in the backward direction.
		layer = 0;
		todoVertices = new IdentityHashSet<>(1); //Reset this set to the initial vertex.
		todoVertices.add(this);
		doneVertices.clear(); //Reset the done flags.
		doneArcs.clear();
		while(!todoVertices.isEmpty()) {
			layer++;
			todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
			for(final Vertex<V,A> vertex : todoVertices) {
				result -= (vertex.label == null ? 0 : net.dulek.math.Math.power(31,layer) * vertex.label.hashCode()) + net.dulek.math.Math.power(vertex.outgoingArcs.size(),layer) + net.dulek.math.Math.power(vertex.incomingArcs.size(),layer);
				for(final Arc<V,A> arc : vertex.incomingArcs) { //Add all unexplored arcs to the todo list.
					if(!doneArcs.contains(arc)) {
						todoArcs.add(arc);
					}
				}
				doneVertices.add(vertex); //Don't visit this vertex ever again.
			}
			todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
			for(final Arc<V,A> arc : todoArcs) {
				result -= (arc.getLabel() == null ? 0 : net.dulek.math.Math.power(127,layer) * arc.getLabel().hashCode()) + net.dulek.math.Math.power(arc.destinationEndpoints().size(),layer) + net.dulek.math.Math.power(arc.sourceEndpoints().size(),layer);
				for(final Vertex<V,A> vertex : arc.sourceEndpoints()) { //Add all unexplored vertices to the todo list.
					if(!doneVertices.contains(vertex)) {
						todoVertices.add(vertex);
					}
				}
				doneArcs.add(arc); //Don't visit this arc ever again.
			}
		}

		return result;
	}

	/**
	 * Returns the set of all arcs going into this vertex. An arc is going into
	 * this vertex if the arc has the vertex in its destination. The source of
	 * the arcs has no effect on this relation between vertices and arcs.
	 * @return The set of all arcs going into this vertex.
	 */
	public Set<Arc<V,A>> incomingArcs() {
		return incomingArcs; //Since the return type is a normal set, the internal methods are not exposed.
	}

	/**
	 * Returns whether another vertex is adjacent to this vertex. A vertex is
	 * adjacent to this vertex if there exists an arc with this vertex in its
	 * source and the other vertex in its destination.
	 * <p>This is equivalent to {@code other.isAdjacentTo(this)}.</p>
	 * <p>This implementation iterates over either the outgoing arcs of this
	 * vertex or the incoming arcs of the specified vertex, whichever is
	 * smaller, and sees whether the other vertex is seen at the opposite end of
	 * one of the arcs.</p>
	 * @param other The vertex of which to check whether it is adjacent to this
	 * vertex.
	 * @return {@code true} if the specified vertex is adjacent to this vertex,
	 * or {@code false} otherwise.
	 */
	@SuppressWarnings("element-type-mismatch") //Caused by checking for containment of net.dulek.collections.graph.Vertex in a set that contains net.dulek.collections.graph.arc.vertex.Vertex instances.
	public boolean isAdjacent(final Vertex<V,A> other) {
		final Set<Arc<V,A>> otherIncoming = other.incomingArcs();
		if(outgoingArcs.size() <= otherIncoming.size()) { //This side has a smaller set, so iterate over this.
			for(final Arc<V,A> arc : outgoingArcs) { //Go through every arc to see if the specified vertex is in its destination.
				if(arc.destinationEndpoints().contains(other)) {
					return true;
				}
			}
			return false; //None of them had the specified vertex in its destination.
		}
		//The other side has a smaller set, so iterate over that.
		for(final Arc<V,A> arc : otherIncoming) { //Go through every arc to see if this vertex is in its source.
			if(arc.sourceEndpoints().contains(this)) {
				return true;
			}
		}
		return false; //None of them had the specified vertex in its source.
	}

	/**
	 * Returns whether this vertex is adjacent to another vertex. This vertex is
	 * adjacent to another vertex if there exists an arc with the other vertex
	 * in its source and this vertex in its destination.
	 * <p>This is equivalent to {@code other.isAdjacent(this)}.</p>
	 * @param other The vertex of which to check whether this vertex is adjacent
	 * to it.
	 * @return {@code true} if this vertex is adjacent to the specified vertex,
	 * or {@code false} otherwise.
	 */
	@SuppressWarnings("element-type-mismatch") //Caused by checking for containment of net.dulek.collections.graph.Vertex in a set that contains net.dulek.collections.graph.arc.vertex.Vertex instances.
	public boolean isAdjacentTo(final Vertex<V,A> other) {
		final Set<Arc<V,A>> otherOutgoing = other.outgoingArcs();
		if(incomingArcs.size() <= otherOutgoing.size()) { //This side has a smaller set, so iterate over this.
			for(final Arc<V,A> arc : incomingArcs) { //Go through every arc to see if the specified vertex is in its source.
				if(arc.sourceEndpoints().contains(other)) {
					return true;
				}
			}
			return false; //None of them had the specified vertex in its source.
		}
		//The other side has a smaller set, so iterate over that.
		for(final Arc<V,A> arc : otherOutgoing) { //Go through every arc to see if this vertex is in its destination.
			if(arc.destinationEndpoints().contains(this)) {
				return true;
			}
		}
		return false; //None of them had the specified vertex in its destination.
	}

	/**
	 * Returns the set of all arcs going out of this vertex. An arc is going out
	 * of this vertex if the arc has the vertex in its source. The destination
	 * of the arcs has no effect on this relation between vertices and arcs.
	 * @return The set of all arcs going out of this vertex.
	 */
	public Set<Arc<V,A>> outgoingArcs() {
		return outgoingArcs; //Since the return type is a normal set, the internal methods are not exposed.
	}

	/**
	 * Returns a path from this vertex to the other, if it exists. A path is an
	 * ordered list of zero or more incident arcs from this vertex to the other.
	 * The path will be listed in order, starting with an arc going out of this
	 * vertex and ending with an arc going into the specified vertex. Each arc
	 * is going out of a vertex in the previous arc's destination.
	 * <p>Note that the path returned may be any path. It needs not be the
	 * shortest possible path. This implementation will find a path using
	 * breadth-first search, resulting in a path with the least amount of arcs.
	 * This is only guaranteed to be the shortest path if the arcs are not
	 * weighted or weighted equally.</p>
	 * <p>If there exists no path from this vertex to the specified vertex,
	 * {@code null} will be returned.</p>
	 * @param other The vertex to find a path to.
	 * @return A path by which the graph can be traversed from this vertex to
	 * the specified vertex, or {@code null} if no such path exists.
	 */
	public List<Arc<V,A>> pathTo(final Vertex<V,A> other) {
		if(this == other) { //Zero-length paths are positives too.
			return new ArrayList<>(1);
		}
		if(graph == null) { //We're not in a graph. We can't reach vertices other than ourselves.
			return null;
		}
		final Queue<Vertex<V,A>> todo = new ArrayDeque<>(graph.numVertices() >> 3); //Perform a breadth-first search. This queue represents the front. Estimate a maximum front size of n/8.
		final Map<Vertex<V,A>,Arc<V,A>> done = new IdentityHashMap<>(graph.numVertices() >> 1); //For every vertex we've seen, remember what arc we came from so that we can backtrack. Estimate to see at most n/2 vertices.
		final Map<Arc<V,A>,Vertex<V,A>> track = new IdentityHashMap<>(graph.numArcs() >> 1); //For every arc we've seen, remember what vertex we came from so that we can backtrack. Estimate to see at most m/2 arcs.
		todo.add(this);
		while(!todo.isEmpty()) { //Keep traversing the graph until no more new vertices are reachable.
			Vertex<V,A> vertex = todo.remove();
			for(Arc<V,A> arc : vertex.outgoingArcs) {
				for(final Vertex<V,A> adjacentVertex : arc.destinationEndpoints()) {
					if(adjacentVertex == other) { //Considered doing this check in the if-statement below, but if the graph is sparse this check won't occur often and adding to a hashmap is expensive.
						final ArrayList<Arc<V,A>> result = new ArrayList<>(graph.numVertices() >> 3); //Track back to the source to find what the path was.
						result.add(arc);
						while(vertex != this) {
							arc = done.get(vertex); //Track back which arc we came from.
							result.add(arc);
							vertex = track.get(arc); //Track back which vertex we came from.
						}
						return result;
					}
					if(done.put(adjacentVertex,arc) == null) { //We've never seen this vertex yet.
						todo.offer(adjacentVertex);
						track.put(arc,vertex); //Keep track of the breadcrumbs!
					}
				}
			}
		}
		return null; //No more vertices to explore. Not reachable then.
	}

	/**
	 * Changes the label in this vertex to the specified label. The previous
	 * label will be returned.
	 * @param label The new label for the vertex.
	 * @return The label of the vertex before this method was called.
	 * @throws IllegalStateException The specified label is not allowed on this
	 * vertex.
	 */
	public V setLabel(final V label) {
		V old = this.label; //Store the old label before we overwrite it.
		this.label = label;
		return old;
	}

	/**
	 * Returns a {@code String} representation of the vertex. The String will
	 * state that it is a vertex, state the label of the vertex, and state the
	 * vertices to which it is connected and by which arcs. To identify the
	 * other vertices and arcs, an artificial indexing is used based on the
	 * creation time of these vertices and arcs. Every vertex and arc is
	 * assigned a unique integer.
	 * <p>The unique identifier will be printed on the first line, followed by a
	 * colon and space and then the string representation of the label on the
	 * vertex and a newline character. The second line will feature the outgoing
	 * arcs of the vertex (again identified by their unique identifiers),
	 * separated by commas.</p>
	 * @return A {@code String} representation of the vertex.
	 */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(13 + (outgoingArcs.size() << 2)); //Reserve 2 chars for the uid, 2 for the colon and space, 8 for the label, 1 for the newline and 4 for each arc.
		result.append(Integer.toString(uid));
		result.append(": ");
		if(label == null) {
			result.append("null");
		} else {
			result.append(label.toString());
		}
		result.append("\n");
		final Iterator<Arc<V,A>> it = outgoingArcs.iterator();
		if(it.hasNext()) { //Don't prepend the first arc with a comma.
			result.append(Integer.toString(it.next().uid));
			while(it.hasNext()) { //Print all the rest with commas before them.
				result.append(',');
				result.append(Integer.toString(it.next().uid));
			}
		}
		return result.toString();
	}

	/**
	 * Adds the specified arc to the set of incoming arcs. The arc is only added
	 * to the set. Other elements of the graph are untouched. If this operation
	 * would cause the graph to become invalid, an {@code IllegalStateException}
	 * will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When an arc is coupled to the vertex, the vertex needs to be coupled to
	 * the arc as well but without trying to couple the arc to the vertex again.
	 * </p>
	 * @param arc The arc to add to the incoming arcs of this vertex.
	 * @throws IllegalStateException Adding the arc to the incoming arcs of this
	 * vertex would cause the graph to become invalid.
	 */
	protected void addToIncomingInternal(final Arc<V,A> arc) {
		incomingArcs.addInternal(arc);
	}

	/**
	 * Adds the specified arc to the set of outgoing arcs. The arc is only added
	 * to the set. Other elements of the graph are untouched. If this operation
	 * would cause the graph to become invalid, an {@code IllegalStateException}
	 * will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When an arc is coupled to the vertex, the vertex needs to be coupled to
	 * the arc as well but without trying to couple the arc to the vertex again.
	 * </p>
	 * @param arc The arc to add to the outgoing arcs of this vertex.
	 * @throws IllegalStateException Adding the arc to the outgoing arcs of this
	 * vertex would cause the graph to become invalid.
	 */
	protected void addToOutgoingInternal(final Arc<V,A> arc) {
		outgoingArcs.addInternal(arc);
	}

	/**
	 * Removes all of the arcs from the set of incoming arcs. Only the set of
	 * incoming arcs is cleared. Other elements of the graph are untouched. If
	 * this operation would cause the graph to become invalid, an
	 * {@code IllegalStateException} will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When all arcs are removed, all arcs of the vertex need to be uncoupled
	 * from the vertex as well without trying to uncouple the vertex from the
	 * arcs again.</p>
	 * @throws IllegalStateException Clearing the incoming arcs of the vertex
	 * would cause the graph to become invalid.
	 */
	protected void clearIncomingInternal() {
		incomingArcs.clearInternal();
	}

	/**
	 * Removes all of the arcs from the set of outgoing arcs. Only the set of
	 * outgoing arcs is cleared. Other elements of the graph are untouched. If
	 * this operation would cause the graph to become invalid, an
	 * {@code IllegalStateException} will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When all arcs are removed, all arcs of the vertex need to be uncoupled
	 * from the vertex as well without trying to uncouple the vertex from the
	 * arcs again.</p>
	 * @throws IllegalStateException Clearing the outgoing arcs of the vertex
	 * would cause the graph to become invalid.
	 */
	protected void clearOutgoingInternal() {
		outgoingArcs.clearInternal();
	}

	/**
	 * Removes this vertex from its outgoing and incoming arcs. This is a helper
	 * method. This will remove the vertex from the source of its outgoing arcs
	 * and the destination of its incoming arcs, preventing any traversal to it.
	 * If this causes an arc to have no vertices in either its source or its
	 * destination any more, or if the graph allows halfarcs and an arc has no
	 * vertices in both its source and its destination any more, those arcs will
	 * also automatically be removed.
	 * <p>The vertex is not removed from the {@code vertices} set of the graph.
	 * For that, the {@link #removeFromSet()} method must be used.</p>
	 * @see #removeFromSet()
	 */
	protected abstract void removeFromGraph();

	/**
	 * Removes the specified arc from the set of incoming arcs. The vertex is
	 * only removed from the set. Other elements of the graph are untouched. If
	 * this operation would cause the graph to become invalid, an
	 * {@code IllegalStateException} will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When an arc is uncoupled from the vertex, the vertex needs to be
	 * uncoupled from the arc as well but without trying to uncouple the arc
	 * from the vertex again.</p>
	 * @param arc The arc to remove from the incoming arcs of this vertex.
	 * @throws IllegalStateException Removing the arc from the incoming arcs of
	 * this vertex would cause the graph to become invalid.
	 */
	protected void removeFromIncomingInternal(final Arc<V,A> arc) {
		incomingArcs.removeInternal(arc);
	}

	/**
	 * Removes the specified arc from the set of outgoing arcs. The vertex is
	 * only removed from the set. Other elements of the graph are untouched. If
	 * this operation would cause the graph to become invalid, an
	 * {@code IllegalStateException} will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When an arc is uncoupled from the vertex, the vertex needs to be
	 * uncoupled from the arc as well but without trying to uncouple the arc
	 * from the vertex again.</p>
	 * @param arc The arc to remove from the outgoing arcs of this vertex.
	 * @throws IllegalStateException Removing the arc from the outgoing arcs of
	 * this vertex would cause the graph to become invalid.
	 */
	protected void removeFromOutgoingInternal(final Arc<V,A> arc) {
		outgoingArcs.removeInternal(arc);
	}

	/**
	 * Removes this vertex from the {@link vertices()} set of the graph. This is
	 * a helper method. The value returned by {@link Graph#numVertices()} will
	 * drop by {@code 1}.
	 * <p>The vertex is not unlinked from the arcs of the graph, so traversal to
	 * it is still possible. To unlink the vertex, the
	 * {@link #removeFromGraph()} method must be used.</p>
	 * @see #removeFromGraph()
	 */
	protected void removeFromSet() {
		if(graph != null) {
			graph.vertices.removeInternal(this);
			graph = null;
		}
	}

	/**
	 * This set contains the arcs that are coming into a vertex. It is returned
	 * by the {@link #incomingArcs()} method. This implementation ensures that
	 * modifying the set will modify the accompanying graph accordingly.
	 */
	protected abstract class IncomingArcSet extends IdentityHashSet<Arc<V,A>> {
		/**
		 * The version of the serialised format of the set. This identifies a
		 * serialisation as being the serialised format of this class. It needs
		 * to be different from the original serialisation of {@link HashSet}
		 * since it cannot contain {@code null}-elements.
		 */
		//private static final long serialVersionUID = 1;

		/**
		 * Adds the specified arc to this set if it is not already present. If
		 * this set already contains the specified arc, the call leaves the set
		 * unchanged and returns {@code false}.
		 * <p>This method will have a corresponding effect on the graph. It will
		 * add the vertex to the set of destination vertices of the arc. If this
		 * violates the restrictions placed on the graph, such as by adding a
		 * second vertex to the destination of an arc where hyperarcs are not
		 * allowed, an {@code IllegalStateException} will be thrown.</p>
		 * @param element The arc to be added to this set.
		 * @return {@code true} if this set did not already contain the
		 * specified arc, or {@code false} otherwise.
		 * @throws IllegalStateException The {@code add(Arc)} operation would
		 * cause the graph to become invalid.
		 * @throws NullPointerException The specified arc to add was
		 * {@code null}.
		 */
		@Override
		public abstract boolean add(final Arc<V,A> element);

		/**
		 * Adds all of the arcs in the specified collection to this set. If the
		 * specified collection is this set, nothing will be changed.
		 * <p>This implementation iterates over the specified collection, and
		 * adds each arc returned by the iterator to this collection, in turn.
		 * </p>
		 * <p>This method will have a corresponding effect on the graph. It will
		 * add the vertex to the set of destination vertices of every arc in the
		 * collection. If this violates the restrictions placed on the graph,
		 * such as by adding a second vertex to the destination of an arc where
		 * hyperarcs are not allowed, an {@code IllegalStateException} will be
		 * thrown.</p>
		 * @param c The collection containing arcs to be added to this set.
		 * @return {@code true} if this set changed as a result of the call.
		 * @throws IllegalStateException The {@code addAll(Collection)}
		 * operation would cause the graph to become invalid.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		public abstract boolean addAll(final Collection<? extends Arc<V,A>> c);

		/**
		 * Removes all of the arcs from this set. The set will be empty after
		 * this call returns.
		 * <p>This method will have a corresponding effect on the graph. It will
		 * remove the vertex from the destination of all arcs in the set. If
		 * this violates the restrictions placed on the graph, such as by
		 * removing the last vertex in the destination of an arc when halfarcs
		 * are not allowed, an {@code IllegalStateException} will be thrown.</p>
		 * @throws IllegalStateException The {@code clear()} operation would
		 * cause the graph to become invalid.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by casting elements of the table to Arc when they're being iterated over.
		public void clear() {
			modCount++;
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			final Object[] newTable = new Object[t.length]; //The new table will be completely filled with nulls.
			for(int i = t.length - 1;i >= 0;i--) {
				final Object elem = t[i];
				if(elem != null && elem != tombstone) {
					try {
						((Arc<V,A>)elem).removeFromDestinationInternal(Vertex.this); //Safe cast, since the table contains only Arcs.
					} catch(IllegalStateException e) {
						for(;i < t.length;i++) { //Count back up from where we were.
							final Arc<V,A> elem2 = (Arc<V,A>)elem; //Unchecked cast is safe since the table contains only Arcs.
							for(Vertex<V,A> vertex : elem2.sourceEndpoints()) { //Re-link all arcs.
								vertex.outgoingArcs.addInternal(elem2);
							}
							for(Vertex<V,A> vertex : elem2.destinationEndpoints()) {
								vertex.incomingArcs.addInternal(elem2);
							}
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			table = newTable; //Everything was properly unlinked without exceptions, so replace the table with the empty one.
		}

		/**
		 * Returns a deep copy of this set of arcs. To achieve this, the entire
		 * graph is copied and the incoming arc set of the corresponding vertex
		 * is returned. The new set will be completely separate from the old
		 * set, in that no references to the graph of the old set are maintained
		 * and that modifying the new set will not modify the old set or its
		 * graph in any way or vice versa.
		 * @return A copy of this set of arcs, which is placed in a copy of the
		 * graph.
		 * @throws CloneNotSupportedException Cloning is supported, but this
		 * allows extensions of this class to not support cloning.
		 */
		@Override
		@SuppressWarnings("CloneDoesntCallSuperClone") //Doesn't call super.clone() directly, but clones an encompassing object of it and returns the new version of this set.
		public IncomingArcSet clone() throws CloneNotSupportedException {
			if(graph == null) {
				return Vertex.this.clone().incomingArcs; //Copy only the vertex, and return its incomingArcs set.
			}
			return graph.clone(Vertex.this).incomingArcs; //Copy the entire graph, and return the incomingArcs set of the corresponding vertex.
		}

		/**
		 * Returns {@code true} if this set contains the specified arc.
		 * @param element The arc whose presence in this set is to be tested.
		 * @return {@code true} if this set contains the specified element, or
		 * {@code false} otherwise.
		 */
		@Override
		public boolean contains(final Object element) {
			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Compute the hash code for its memory address.

			int offset = 1;
			Object elem;
			while((elem = t[hash]) != null) { //Search until we hit an empty spot.
				if(elem == element) { //This is the element we seek.
					return true;
				}
				hash = (hash + offset++) & tMax;
			}
			return false; //Reached an empty spot and haven't found it yet.
		}

		/**
		 * Returns an iterator over the arcs in this set. The arcs are returned
		 * in no particular order, as the order depends on the order in which
		 * they are stored in the hash table, and this is unspecified.
		 * <p>Note that the iterator will search through all buckets of the hash
		 * table to search for the elements of the set. The time complexity of a
		 * complete iteration of the set scales with the capacity of the hash
		 * table plus the number of elements in the set. Also, the time between
		 * two consecutive calls to {@link Iterator#next()} may vary.</p>
		 * <p>The iterator is fail-fast, which means that if a structural
		 * modification is made to the hash set after this method call, the
		 * iterator returned by this method call will throw a
		 * {@link ConcurrentModificationException} when {@link Iterator#next()}
		 * or {@link Iterator#remove()} is called. This behaviour prevents
		 * nondeterministic or unexpected behaviour caused by the concurrent
		 * modification of the set while it is being iterated over. Rather than
		 * giving inconsequent results, it always fails to give a result.</p>
		 * @return An iterator over the arcs in this set.
		 */
		@Override
		public Iterator<Arc<V,A>> iterator() {
			return new Itr();
		}

		/**
		 * Removes the specified arc from this set if it is present. Returns
		 * {@code true} if this set contained the arc (or equivalently, if this
		 * set changed as a result of the call). This set will not contain the
		 * arc once the call returns.
		 * <p>This method will have a corresponding effect on the graph. It will
		 * remove the vertex from the destination of the specified arc. If this
		 * violates the restrictions placed on the graph, such as by removing
		 * the last vertex in the destination of an arc when halfarcs are not
		 * allowed, an {@code IllegalStateException} will be thrown.</p>
		 * @param element The arc to be removed from this set, if present.
		 * @return {@code true} if the set contained the specified arc, or
		 * {@code false} otherwise.
		 * @throws IllegalStateException The {@code remove(Object)} operation
		 * would cause the graph to become invalid.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by casting elements of the table to Arc when the specified element is found.
		public boolean remove(final Object element) {
			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Compute the hash code for its memory address.
			Object elem;
			int offset = 1;
			while((elem = t[hash]) != null) { //Look for the object.
				if(elem == element) { //This is the element we seek.
					((Arc<V,A>)elem).removeFromDestinationInternal(Vertex.this); //Safe cast, since the table contains only Arcs.
					t[hash] = tombstone; //R.I.P.
					modCount++;
					size--;
					return true;
				}
				hash = (hash + offset++) & tMax;
			}
			return false;
		}

		/**
		 * Removes from this set all of its arcs that are contained in the
		 * specified collection. If the specified collection is also a set, this
		 * operation effectively modifies this set so that its value is the
		 * asymmetric set difference of the two sets.
		 * <p>This implementation iterates over either the specified collection
		 * or over the set, based on which is smaller: the size of the
		 * collection or the capacity of the hash table. Since the hash table
		 * iterates over the total size of the table rather than just the
		 * elements, its table capacity is compared rather than the cardinality
		 * of the set. When iterating over the collection, each element of the
		 * collection is removed from the set if it is present. When iterating
		 * over the set, each element of the set is removed from the set if it
		 * is contained in the collection.</p>
		 * <p>This method will have a corresponding effect on the graph. It will
		 * remove the vertices from the destination of the specified arcs. If
		 * this violates the restrictions placed on the graph, such as by
		 * removing the last vertex in the destination of an arc when halfarcs
		 * are not allowed, an {@code IllegalStateException} will be thrown.</p>
		 * <p>Note that these arcs are removed one by one from the graph, at
		 * each arc checking if the graph is still legal. It is concievable that
		 * constraints on a graph exist that would make removing a certain set
		 * of arcs legal, but removing some subsets of that set illegal. In
		 * those cases, this method should be overridden to first attempt to
		 * remove all arcs, and then checking at once if the graph is still
		 * legal (and if it is not, undo the changes and throw an
		 * {@code IllegalStateException}).</p>
		 * @param c The collection containing arcs to be removed from this set.
		 * @return {@code true} if this set changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ClassCastException The class of an element of the specified
		 * collection is not a subclass of {@code Arc}.
		 * @throws IllegalStateException The {@code removeAll} operation caused
		 * the graph to become invalid.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@SuppressWarnings("unchecked") //Caused by casting elements of the table to Arc when the specified element is found.
		@Override
		public boolean removeAll(final Collection<?> c) {
			if(c == null) {
				throw new NullPointerException("The specified collection to remove all elements from is null.");
			}
			final Object[] t = table; //Local cache for speed without JIT.
			int numRemovedElements = 0;
			final Object[] removedElements = new Object[Math.min(c.size(),size)]; //We need to be able to undo the call in the event of an exception, so keep track of the removed elements.

			//Pick whichever method is fastest:
			if(table.length > c.size() || (c instanceof List && table.length > Math.sqrt(c.size()))) { //Iterate over c, removing all elements from this set. Lists have linear contains() methods, so they get special treatment.
				final int tMax = t.length - 1;

				for(final Object element : c) {
					//Compute the desired bucket for the element.
					int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

					Object elem;
					int offset = 1;
					while((elem = t[hash]) != null) { //Look for the object.
						if(elem == element) { //This is the element we seek. Will never be true for null elements.
							removedElements[numRemovedElements++] = element;
							t[hash] = tombstone; //R.I.P.
							try {
								((Arc<V,A>)element).removeFromDestinationInternal(Vertex.this); //Safe cast, since the hash table contained this element and it contains only Arcs.
							} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
								for(int i = numRemovedElements - 1;i >= 0;i--) {
									add((Arc<V,A>)removedElements[i]); //Add it back to the table. Safe cast, since they have all been in the hash table, and the table contains only Arcs.
								}
								throw e; //Pass the original exception on.
							}
							break;
						}
						hash = (hash + offset++) & tMax;
					}
					//Element not found. Continue with the next element.
				}
				if(numRemovedElements >= 0) { //That actually removed something.
					modCount++;
					size -= numRemovedElements;
					return true;
				}
				return false;
			}
			//Otherwise, iterate over the set, removing all vertices that are in c.
			for(int i = t.length - 1;i >= 0;i--) {
				final Object elem;
				if((elem = t[i]) != null && elem != tombstone && c.contains(elem)) { //Check if c has this element.
					removedElements[numRemovedElements++] = elem;
					t[i] = tombstone; //R.I.P.
					try {
						((Arc<V,A>)elem).removeFromDestinationInternal(Vertex.this); //Safe cast, since the hash table contains only Arcs.
					} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(int j = numRemovedElements - 1;j >= 0;j--) {
							add((Arc<V,A>)removedElements[j]); //Add it back to the table. Safe cast, since they have all been in the hash table, and the table contains only Arcs.
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			if(numRemovedElements >= 0) { //That actually removed something.
				modCount++;
				size -= numRemovedElements;
				return true;
			}
			return false;
		}

		/**
		 * Retains only the arcs in this set that are contained in the specified
		 * collection. In other words, removes from this set all of its arcs
		 * that are not contained in the specified collection. When the method
		 * call returns, the set will contain the intersection of the original
		 * arcs of the set and the arcs of the collection.
		 * <p>This implementation iterates over the arcs of the set and checks
		 * for each arc whether it is contained in the specified collection,
		 * removing it if it is not contained.</p>
		 * <p>List collections get special treatment. Their
		 * {@link List#contains(Object)} method is generally linear-time and
		 * their iterator constant per element. Therefore, when encountered with
		 * a list of reasonable size, this method will instead clear the set and
		 * then iterate over the list, re-adding those arcs that were in the
		 * original set.</p>
		 * <p>This method will have a corresponding effect on the graph. It will
		 * remove the vertices from the destination of the specified arcs. If
		 * this violates the restrictions placed on the graph, such as by
		 * removing the last vertex in the source of an arc when halfarcs are
		 * not allowed, an {@code IllegalStateException} will be thrown.</p>
		 * <p>Note that these arcs are removed one by one from the graph, at
		 * each arc checking if the graph is still legal. It is concievable that
		 * constraints on a graph exist that would make removing a certain set
		 * of arcs legal, but removing some subsets of that set illegal. In
		 * those cases, this method should be overridden to first attempt to
		 * remove all arcs, and then checking at once if the graph is still
		 * legal (and if it is not, undo the changes and throw an
		 * {@code IllegalStateException}).</p>
		 * @param c The collection to retain the arcs from.
		 * @return {@code true} if this set changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ClassCastException The class of an element of the specified
		 * collection is not a subclass of {@code Arc}.
		 * @throws IllegalStateException The {@code retainAll} operation caused
		 * the graph to become invalid.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by casting elements of the table to Arc.
		public boolean retainAll(final Collection<?> c) {
			if(c == null) {
				throw new NullPointerException("The specified collection to retain all arcs from is null.");
			}
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.

			if(c instanceof List && t.length > Math.sqrt(c.size())) { //Lists have linear contains() methods and will get special treatment.
				//Iterate over the list and add all elements that are in both the list and in the original table.
				final Object[] newTable = new Object[t.length];
				final int tMax = t.length - 1;
				int numRetainedElements = 0;

				for(Object element : c) { //See if it's in the original hash table.
					//Compute the desired bucket for the element.
					int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

					Object elem;
					int offset = 1;
					while((elem = t[hash]) != null) { //Look for the arc.
						if(elem == tombstone) { //Copy over tombstones as well, or we'd have to rehash everything.
							newTable[hash] = elem;
						} else if(elem == element) { //Found it! Will never be true for null elements.
							newTable[hash] = elem; //Copy it to the new table.
							t[hash] = tombstone; //Don't find it a second time.
							numRetainedElements++;
							break;
						}
						hash = (hash + offset++) & tMax;
					}
					//Element not found. Continue with the next element.
				}
				int numRemovedElements = 0;
				final Object[] removedElements = new Object[size - numRetainedElements]; //We need to be able to undo changes in the event of an exception, so keep track of the removed elements.
				for(int i = tMax;i >= 0;i--) { //Place tombstones in the new table where arcs have been removed.
					Object elem;
					if((elem = t[i]) != null && newTable[i] == null) {
						newTable[i] = tombstone;
						removedElements[numRemovedElements++] = elem;
						try {
							((Arc<V,A>)elem).removeFromDestinationInternal(Vertex.this); //Safe cast, since the hash table contains only Arcs.
						} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
							for(int j = numRemovedElements - 1;j >= 0;j--) {
								add((Arc<V,A>)removedElements[j]); //Re-add the arc. Safe cast, since all these objects were in the table and the table has only Arcs.
							}
							throw e; //Pass the original exception on.
						}
					}
				}
				table = newTable; //Use the new table. The old one was modified anyhow.
				if(numRetainedElements < size) { //That actually removed something.
					modCount++;
					size = numRetainedElements;
					return true;
				}
				return false;
			}

			int numRemovedElements = 0;
			final Object[] removedElements = new Object[Math.min(size,c.size())]; //We need to be able to undo the changes in the event of an exception, so keep track of the removed elements.
			for(int i = t.length - 1;i >= 0;i--) {
				final Object elem;
				if((elem = t[i]) != null && elem != tombstone && !c.contains(elem)) { //Check if c contains the element.
					t[i] = tombstone; //R.I.P.
					removedElements[numRemovedElements++] = elem;
					try {
						((Arc<V,A>)elem).removeFromDestinationInternal(Vertex.this); //Safe cast, since the hash table contains only Arcs.
					} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(int j = numRemovedElements - 1;j >= 0;j--) {
							add((Arc<V,A>)removedElements[j]); //Re-add the arc. Safe cast, since all these objects were in the table and the table has only Arcs.
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			if(numRemovedElements > 0) { //That actually removed something.
				modCount++;
				size -= numRemovedElements;
				return true;
			}
			return false;
		}

		/**
		 * Adds the specified arc to this set if it is not already present. If
		 * this set already contains the specified arc, the call leaves the set
		 * unchanged and returns {@code false}. The arc is just added to the set
		 * itself. The linkage in the graph will not be manipulated.
		 * @param element The arc to be added to this set.
		 * @return {@code true} if this set did not already contain the
		 * specified arc, or {@code false} otherwise.
		 * @throws NullPointerException The specified arc to add was
		 * {@code null}.
		 */
		protected boolean addInternal(final Arc<V,A> element) {
			Object object = element;
			if(object == null) { //Don't try to add null. It's not allowed in this set.
				throw new NullPointerException("Trying to add null to an OutgoingArcSet.");
			}
			final int tMax = table.length - 1;
			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(object) & tMax; //Compute the hash code for its memory address.

			int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
			Object elem;
			//Search for the element.
			while((elem = table[hash]) != null) {
				if(elem == tombstone) { //There was an object here...
					//Continue the search, but no more gravedigging.
					int searchIndex = (hash + offset++) & tMax;
					while((elem = table[searchIndex]) != null) {
						if(elem == object) { //The element is already in the table.
							return false;
						}
						searchIndex = (searchIndex + offset++) & tMax;
					}
					table[hash] = object; //Place it at the tombstone.
					modCount++;
					if(++size > treshold) { //Getting too big.
						resize(table.length << 1);
					}
					return true;
				}
				if(elem == object) { //The element is already in the table.
					return false;
				}
				hash = (hash + offset++) & tMax;
			}
			//table[hash] is now null (since the while loop ended) so this is a free spot.
			table[hash] = object; //Place it at our free spot.
			modCount++;
			if(++size > treshold) { //Getting too big.
				resize(table.length << 1);
			}
			return true;
		}

		/**
		 * Removes all of the arcs from this set. The set will be empty after
		 * this call returns. Only the set itself is cleared. The linkage in the
		 * graph will not be manipulated.
		 */
		protected void clearInternal() {
			super.clear();
		}

		/**
		 * Removes the specified arc from this set if it is present. Returns
		 * {@code true} if this set contained the specified arc (or
		 * equivalently, if this set changed as a result of the call). This set
		 * will not contain the arc once the call returns. The arc is just
		 * removed from the set itself. The linkage in the graph will not be
		 * manipulated.
		 * @param element The arc to be removed from this set, if present.
		 * @return {@code true} if the set contained the specified arc, or
		 * {@code false} otherwise.
		 */
		protected boolean removeInternal(Object element) {
			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

			Object elem;
			int offset = 1;
			while((elem = t[hash]) != null) { //Look for the arc.
				if(elem == element) { //This is the arc we seek. Will never be true for null input.
					//So don't unlink it in the graph.
					t[hash] = tombstone; //R.I.P.
					modCount++;
					size--;
					return true;
				}
				hash = (hash + offset++) & tMax;
			}
			return false; //Reached an empty spot without any hit. Not found.
		}

		/**
		 * This iterator iterates over the arcs of a hash set. The iterator is a
		 * minor modification of the original {@link HashSet.Itr}
		 * implementation. The only difference it that removing an element
		 * through this iterator will also unlink it from the vertex the set
		 * belongs to that the iterator belongs to. The iterator gives the
		 * elements of the set in no particular order. The actual order will be
		 * the reverse order of the vertices in the set as they are ordered in
		 * the internal hash table of the set.
		 * <p>Note that the iterator will search through all buckets of the hash
		 * table to search for the elements of the set. The time complexity of a
		 * complete iteration of the set scales with the capacity of the hash
		 * table plus the number of elements in the set. Also, the time between
		 * two consecutive calls to {@link Iterator#next()} may vary.</p>
		 * <p>The iterator is fail-fast, which means that if a structural
		 * modification is made to the hash set after this method call, the
		 * iterator will throw a {@link ConcurrentModificationException} when
		 * {@link #next()} or {@link #remove()} is called. This behaviour
		 * prevents nondeterministic or unexpected behaviour caused by the
		 * concurrent modification of the set while it is being iterated over.
		 * Rather than giving inconsequent results, it always fails to give a
		 * result.</p>
		 */
		protected class Itr extends HashSet<Arc<V,A>>.Itr {
			/**
			 * Removes from the set the last element returned by this iterator.
			 * This method can be called only once per call to {@link #next()}.
			 * <p>The arc is also unlinked from the vertex in the graph.</p>
			 * @throws ConcurrentModificationException The set was structurally
			 * modified between the constructing of this iterator and the
			 * calling of this method.
			 * @throws IllegalStateException The {@link #next()} method has not
			 * yet been called, or the {@code remove()} method has already been
			 * called after the last call to the {@code next()} method.
			 */
			@Override
			@SuppressWarnings("unchecked") //Caused by the casting of the element to Arc to remove it from the graph.
			public void remove() {
				if(last < 0) { //Nothing to remove.
					throw new IllegalStateException();
				}
				if(modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
				//Delete the element at 'last'.
				final Object elem = table[last];
				if(elem != null && elem != tombstone) {
					((Arc<V,A>)elem).removeFromDestinationInternal(Vertex.this); //Remove the vertex from the arc's source. Safe cast, since the set has only arcs.
					table[last] = tombstone; //R.I.P.
					size--;
					modCount++;
					expectedModCount = modCount; //This modification is expected since the iterator made it.
					last = -1; //Set this to negative so the next call doesn't try to remove it again.
				}
			}
		}
	}

	/**
	 * This set contains the arcs that are going out of a vertex. It is returned
	 * by the {@link #outgoingArcs()} method. This implementation ensures that
	 * modifying the set will modify the accompanying graph accordingly.
	 */
	protected abstract class OutgoingArcSet extends IdentityHashSet<Arc<V,A>> {
		/**
		 * The version of the serialised format of the set. This identifies a
		 * serialisation as being the serialised format of this class. It needs
		 * to be different from the original serialisation of {@link HashSet}
		 * since it cannot contain {@code null}-elements.
		 */
		//private static final long serialVersionUID = 1;

		/**
		 * Adds the specified arc to this set if it is not already present. If
		 * this set already contains the specified arc, the call leaves the set
		 * unchanged and returns {@code false}.
		 * <p>This method will have a corresponding effect on the graph. It will
		 * add the vertex to the set of source vertices of the arc. If this
		 * violates the restrictions placed on the graph, such as by adding a
		 * second vertex to the source of an arc where hyperarcs are not
		 * allowed, an {@code IllegalStateException} will be thrown.</p>
		 * @param element The arc to be added to this set.
		 * @return {@code true} if this set did not already contain the
		 * specified arc, or {@code false} otherwise.
		 * @throws IllegalStateException The {@code add(Arc)} operation would
		 * cause the graph to become invalid.
		 * @throws NullPointerException The specified arc to add was
		 * {@code null}.
		 */
		@Override
		public abstract boolean add(final Arc<V,A> element);

		/**
		 * Adds all of the arcs in the specified collection to this set. If the
		 * specified collection is this set, nothing will be changed.
		 * <p>This implementation iterates over the specified collection, and
		 * adds each arc returned by the iterator to this collection, in turn.
		 * </p>
		 * <p>This method will have a corresponding effect on the graph. It will
		 * add the vertex to the set of source vertices of every arc in the
		 * collection. If this violates the restrictions placed on the graph,
		 * such as by adding a second vertex to the source of an arc where
		 * hyperarcs are not allowed, an {@code IllegalStateException} will be
		 * thrown.</p>
		 * @param c The collection containing arcs to be added to this set.
		 * @return {@code true} if this set changed as a result of the call.
		 * @throws IllegalStateException The {@code addAll(Collection)}
		 * operation would cause the graph to become invalid.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		public abstract boolean addAll(final Collection<? extends Arc<V,A>> c);

		/**
		 * Removes all of the arcs from this set. The set will be empty after
		 * this call returns.
		 * <p>This method will have a corresponding effect on the graph. It will
		 * remove the vertex from the source of all arcs in the set. If this
		 * violates the restrictions placed on the graph, such as by removing
		 * the last vertex in the source of an arc when halfarcs are not
		 * allowed, an {@code IllegalStateException} will be thrown.</p>
		 * @throws IllegalStateException The {@code clear()} operation would
		 * cause the graph to become invalid.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by casting elements of the table to Arc when they're being iterated over.
		public void clear() {
			modCount++;
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			final Object[] newTable = new Object[t.length]; //The new table will be completely filled with nulls.
			for(int i = t.length - 1;i >= 0;i--) {
				final Object elem = t[i];
				if(elem != null && elem != tombstone) {
					try {
						((Arc<V,A>)elem).removeFromSourceInternal(Vertex.this); //Safe cast, since the table contains only Arcs.
					} catch(IllegalStateException e) {
						for(;i < t.length;i++) { //Count back up from where we were.
							final Arc<V,A> elem2 = (Arc<V,A>)elem; //Unchecked cast is safe since the table contains only Arcs.
							for(Vertex<V,A> vertex : elem2.sourceEndpoints()) {
								vertex.outgoingArcs.addInternal(elem2);
							}
							for(Vertex<V,A> vertex : elem2.destinationEndpoints()) {
								vertex.incomingArcs.addInternal(elem2);
							}
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			table = newTable; //Everything was properly unlinked without exceptions, so replace the table with the empty one.
		}

		/**
		 * Returns a deep copy of this set of arcs. To achieve this, the entire
		 * graph is copied and the outgoing arc set of the corresponding vertex
		 * is returned. The new set will be completely separate from the old
		 * set, in that no references to the graph of the old set are maintained
		 * and that modifying the new set will not modify the old set or its
		 * graph in any way or vice versa.
		 * @return A copy of this set of arcs, which is placed in a copy of the
		 * graph.
		 * @throws CloneNotSupportedException Cloning is supported, but this
		 * allows extensions of this class to not support cloning.
		 */
		@Override
		@SuppressWarnings("CloneDoesntCallSuperClone") //Doesn't call super.clone() directly, but clones an encompassing object of it and returns the new version of this set.
		public OutgoingArcSet clone() throws CloneNotSupportedException {
			if(graph == null) {
				return Vertex.this.clone().outgoingArcs; //Clone only the vertex, and return its outgoingArcs set.
			}
			return graph.clone(Vertex.this).outgoingArcs; //Clone the entire graph, and return the outgoingArcs set of the corresponding vertex.
		}

		/**
		 * Returns {@code true} if this set contains the specified arc.
		 * @param element The arc whose presence in this set is to be tested.
		 * @return {@code true} if this set contains the specified element, or
		 * {@code false} otherwise.
		 */
		@Override
		public boolean contains(final Object element) {
			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Compute the hash code for its memory address.

			int offset = 1;
			Object elem;
			while((elem = t[hash]) != null) { //Search until we hit an empty spot.
				if(elem == element) { //This is the element we seek.
					return true;
				}
				hash = (hash + offset++) & tMax;
			}
			return false; //Reached an empty spot and haven't found it yet.
		}

		/**
		 * Returns an iterator over the arcs in this set. The arcs are returned
		 * in no particular order, as the order depends on the order in which
		 * they are stored in the hash table, and this is unspecified.
		 * <p>Note that the iterator will search through all buckets of the hash
		 * table to search for the elements of the set. The time complexity of a
		 * complete iteration of the set scales with the capacity of the hash
		 * table plus the number of elements in the set. Also, the time between
		 * two consecutive calls to {@link Iterator#next()} may vary.</p>
		 * <p>The iterator is fail-fast, which means that if a structural
		 * modification is made to the hash set after this method call, the
		 * iterator returned by this method call will throw a
		 * {@link ConcurrentModificationException} when {@link Iterator#next()}
		 * or {@link Iterator#remove()} is called. This behaviour prevents
		 * nondeterministic or unexpected behaviour caused by the concurrent
		 * modification of the set while it is being iterated over. Rather than
		 * giving inconsequent results, it always fails to give a result.</p>
		 * @return An iterator over the arcs in this set.
		 */
		@Override
		public Iterator<Arc<V,A>> iterator() {
			return new Itr();
		}

		/**
		 * Removes the specified arc from this set if it is present. Returns
		 * {@code true} if this set contained the arc (or equivalently, if this
		 * set changed as a result of the call). This set will not contain the
		 * arc once the call returns.
		 * <p>This method will have a corresponding effect on the graph. It will
		 * remove the vertex from the source of the specified arc. If this
		 * violates the restrictions placed on the graph, such as by removing
		 * the last vertex in the source of an arc when halfarcs are not
		 * allowed, an {@code IllegalStateException} will be thrown.</p>
		 * @param element The arc to be removed from this set, if present.
		 * @return {@code true} if the set contained the specified arc, or
		 * {@code false} otherwise.
		 * @throws IllegalStateException The {@code remove(Object)} operation
		 * would cause the graph to become invalid.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by casting elements of the table to Arc when the specified element is found.
		public boolean remove(final Object element) {
			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Compute the hash code for its memory address.
			Object elem;
			int offset = 1;
			while((elem = t[hash]) != null) { //Look for the object.
				if(elem == element) { //This is the element we seek.
					((Arc<V,A>)elem).removeFromSourceInternal(Vertex.this); //Safe cast, since the table contains only Arcs.
					t[hash] = tombstone; //R.I.P.
					modCount++;
					size--;
					return true;
				}
				hash = (hash + offset++) & tMax;
			}
			return false;
		}

		/**
		 * Removes from this set all of its arcs that are contained in the
		 * specified collection. If the specified collection is also a set, this
		 * operation effectively modifies this set so that its value is the
		 * asymmetric set difference of the two sets.
		 * <p>This implementation iterates over either the specified collection
		 * or over the set, based on which is smaller: the size of the
		 * collection or the capacity of the hash table. Since the hash table
		 * iterates over the total size of the table rather than just the
		 * elements, its table capacity is compared rather than the cardinality
		 * of the set. When iterating over the collection, each element of the
		 * collection is removed from the set if it is present. When iterating
		 * over the set, each element of the set is removed from the set if it
		 * is contained in the collection.</p>
		 * <p>This method will have a corresponding effect on the graph. It will
		 * remove the vertices from the source of the specified arcs. If this
		 * violates the restrictions placed on the graph, such as by removing
		 * the last vertex in the source of an arc when halfarcs are not
		 * allowed, an {@code IllegalStateException} will be thrown.</p>
		 * <p>Note that these arcs are removed one by one from the graph, at
		 * each arc checking if the graph is still legal. It is concievable that
		 * constraints on a graph exist that would make removing a certain set
		 * of arcs legal, but removing some subsets of that set illegal. In
		 * those cases, this method should be overridden to first attempt to
		 * remove all arcs, and then checking at once if the graph is still
		 * legal (and if it is not, undo the changes and throw an
		 * {@code IllegalStateException}).</p>
		 * @param c The collection containing arcs to be removed from this set.
		 * @return {@code true} if this set changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ClassCastException The class of an element of the specified
		 * collection is not a subclass of {@code Arc}.
		 * @throws IllegalStateException The {@code removeAll} operation caused
		 * the graph to become invalid.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@SuppressWarnings("unchecked") //Caused by casting elements of the table to Arc when the specified element is found.
		@Override
		public boolean removeAll(final Collection<?> c) {
			if(c == null) {
				throw new NullPointerException("The specified collection to remove all elements from is null.");
			}
			final Object[] t = table; //Local cache for speed without JIT.
			int numRemovedElements = 0;
			final Object[] removedElements = new Object[Math.min(c.size(),size)]; //We need to be able to undo the call in the event of an exception, so keep track of the removed elements.

			//Pick whichever method is fastest:
			if(table.length > c.size() || (c instanceof List && table.length > Math.sqrt(c.size()))) { //Iterate over c, removing all elements from this set. Lists have linear contains() methods, so they get special treatment.
				final int tMax = t.length - 1;

				for(final Object element : c) {
					//Compute the desired bucket for the element.
					int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

					Object elem;
					int offset = 1;
					while((elem = t[hash]) != null) { //Look for the object.
						if(elem == element) { //This is the element we seek. Will never be true for null elements.
							removedElements[numRemovedElements++] = element;
							t[hash] = tombstone; //R.I.P.
							try {
								((Arc<V,A>)element).removeFromSourceInternal(Vertex.this); //Safe cast, since the hash table contained this element and it contains only Arcs.
							} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
								for(int i = numRemovedElements - 1;i >= 0;i--) {
									add((Arc<V,A>)removedElements[i]); //Add it back to the table. Safe cast, since they have all been in the hash table, and the table contains only Arcs.
								}
								throw e; //Pass the original exception on.
							}
							break;
						}
						hash = (hash + offset++) & tMax;
					}
					//Element not found. Continue with the next element.
				}
				if(numRemovedElements >= 0) { //That actually removed something.
					modCount++;
					size -= numRemovedElements;
					return true;
				}
				return false;
			}
			//Otherwise, iterate over the set, removing all vertices that are in c.
			for(int i = t.length - 1;i >= 0;i--) {
				final Object elem;
				if((elem = t[i]) != null && elem != tombstone && c.contains(elem)) { //Check if c has this element.
					removedElements[numRemovedElements++] = elem;
					t[i] = tombstone; //R.I.P.
					try {
						((Arc<V,A>)elem).removeFromSourceInternal(Vertex.this); //Safe cast, since the hash table contains only Arcs.
					} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(int j = numRemovedElements - 1;j >= 0;j--) {
							add((Arc<V,A>)removedElements[j]); //Add it back to the table. Safe cast, since they have all been in the hash table, and the table contains only Arcs.
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			if(numRemovedElements >= 0) { //That actually removed something.
				modCount++;
				size -= numRemovedElements;
				return true;
			}
			return false;
		}

		/**
		 * Retains only the arcs in this set that are contained in the specified
		 * collection. In other words, removes from this set all of its arcs
		 * that are not contained in the specified collection. When the method
		 * call returns, the set will contain the intersection of the original
		 * arcs of the set and the arcs of the collection.
		 * <p>This implementation iterates over the arcs of the set and checks
		 * for each arc whether it is contained in the specified collection,
		 * removing it if it is not contained.</p>
		 * <p>List collections get special treatment. Their
		 * {@link List#contains(Object)} method is generally linear-time and
		 * their iterator constant per element. Therefore, when encountered with
		 * a list of reasonable size, this method will instead clear the set and
		 * then iterate over the list, re-adding those arcs that were in the
		 * original set.</p>
		 * <p>This method will have a corresponding effect on the graph. It will
		 * remove the vertices from the source of the specified arcs. If this
		 * violates the restrictions placed on the graph, such as by removing
		 * the last vertex in the source of an arc when halfarcs are not
		 * allowed, an {@code IllegalStateException} will be thrown.</p>
		 * <p>Note that these arcs are removed one by one from the graph, at
		 * each arc checking if the graph is still legal. It is concievable that
		 * constraints on a graph exist that would make removing a certain set
		 * of arcs legal, but removing some subsets of that set illegal. In
		 * those cases, this method should be overridden to first attempt to
		 * remove all arcs, and then checking at once if the graph is still
		 * legal (and if it is not, undo the changes and throw an
		 * {@code IllegalStateException}).</p>
		 * @param c The collection to retain the arcs of.
		 * @return {@code true} if this set changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ClassCastException The class of an element of the specified
		 * collection is not a subclass of {@code Arc}.
		 * @throws IllegalStateException The {@code retainAll} operation caused
		 * the graph to become invalid.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by casting elements of the table to Arc.
		public boolean retainAll(final Collection<?> c) {
			if(c == null) {
				throw new NullPointerException("The specified collection to retain all arcs from is null.");
			}
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.

			if(c instanceof List && t.length > Math.sqrt(c.size())) { //Lists have linear contains() methods and will get special treatment.
				//Iterate over the list and add all elements that are in both the list and in the original table.
				final Object[] newTable = new Object[t.length];
				final int tMax = t.length - 1;
				int numRetainedElements = 0;

				for(Object element : c) { //See if it's in the original hash table.
					//Compute the desired bucket for the element.
					int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

					Object elem;
					int offset = 1;
					while((elem = t[hash]) != null) { //Look for the arc.
						if(elem == tombstone) { //Copy over tombstones as well, or we'd have to rehash everything.
							newTable[hash] = elem;
						} else if(elem == element) { //Found it! Will never be true for null elements.
							newTable[hash] = elem; //Copy it to the new table.
							t[hash] = tombstone; //Don't find it a second time.
							numRetainedElements++;
							break;
						}
						hash = (hash + offset++) & tMax;
					}
					//Element not found. Continue with the next element.
				}
				int numRemovedElements = 0;
				final Object[] removedElements = new Object[size - numRetainedElements]; //We need to be able to undo changes in the event of an exception, so keep track of the removed elements.
				for(int i = tMax;i >= 0;i--) { //Place tombstones in the new table where arcs have been removed.
					Object elem;
					if((elem = t[i]) != null && newTable[i] == null) {
						newTable[i] = tombstone;
						removedElements[numRemovedElements++] = elem;
						try {
							((Arc<V,A>)elem).removeFromSourceInternal(Vertex.this); //Safe cast, since the hash table contains only Arcs.
						} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
							for(int j = numRemovedElements - 1;j >= 0;j--) {
								add((Arc<V,A>)removedElements[j]); //Re-add the arc. Safe cast, since all these objects were in the table and the table has only Arcs.
							}
							throw e; //Pass the original exception on.
						}
					}
				}
				table = newTable; //Use the new table. The old one was modified anyhow.
				if(numRetainedElements < size) { //That actually removed something.
					modCount++;
					size = numRetainedElements;
					return true;
				}
				return false;
			}

			int numRemovedElements = 0;
			final Object[] removedElements = new Object[Math.min(size,c.size())]; //We need to be able to undo the changes in the event of an exception, so keep track of the removed elements.
			for(int i = t.length - 1;i >= 0;i--) {
				final Object elem;
				if((elem = t[i]) != null && elem != tombstone && !c.contains(elem)) { //Check if c contains the element.
					t[i] = tombstone; //R.I.P.
					removedElements[numRemovedElements++] = elem;
					try {
						((Arc<V,A>)elem).removeFromSourceInternal(Vertex.this); //Safe cast, since the hash table contains only Arcs.
					} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(int j = numRemovedElements - 1;j >= 0;j--) {
							add((Arc<V,A>)removedElements[j]); //Re-add the arc. Safe cast, since all these objects were in the table and the table has only Arcs.
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			if(numRemovedElements > 0) { //That actually removed something.
				modCount++;
				size -= numRemovedElements;
				return true;
			}
			return false;
		}

		/**
		 * Adds the specified arc to this set if it is not already present. If
		 * this set already contains the specified arc, the call leaves the set
		 * unchanged and returns {@code false}. The arc is just added to the set
		 * itself. The linkage in the graph will not be manipulated.
		 * @param element The arc to be added to this set.
		 * @return {@code true} if this set did not already contain the
		 * specified arc, or {@code false} otherwise.
		 * @throws NullPointerException The specified arc to add was
		 * {@code null}.
		 */
		protected boolean addInternal(final Arc<V,A> element) {
			Object object = element;
			if(object == null) { //Don't try to add null. It's not allowed in this set.
				throw new NullPointerException("Trying to add null to an OutgoingArcSet.");
			}
			final int tMax = table.length - 1;
			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(object) & tMax; //Compute the hash code for its memory address.

			int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
			Object elem;
			//Search for the element.
			while((elem = table[hash]) != null) {
				if(elem == tombstone) { //There was an object here...
					//Continue the search, but no more gravedigging.
					int searchIndex = (hash + offset++) & tMax;
					while((elem = table[searchIndex]) != null) {
						if(elem == object) { //The element is already in the table.
							return false;
						}
						searchIndex = (searchIndex + offset++) & tMax;
					}
					table[hash] = object; //Place it at the tombstone.
					modCount++;
					if(++size > treshold) { //Getting too big.
						resize(table.length << 1);
					}
					return true;
				}
				if(elem == object) { //The element is already in the table.
					return false;
				}
				hash = (hash + offset++) & tMax;
			}
			//table[hash] is now null (since the while loop ended) so this is a free spot.
			table[hash] = object; //Place it at our free spot.
			modCount++;
			if(++size > treshold) { //Getting too big.
				resize(table.length << 1);
			}
			return true;
		}

		/**
		 * Removes all of the arcs from this set. The set will be empty after
		 * this call returns. Only the set itself is cleared. The linkage in the
		 * graph will not be manipulated.
		 */
		protected void clearInternal() {
			super.clear();
		}

		/**
		 * Removes the specified arc from this set if it is present. Returns
		 * {@code true} if this set contained the specified arc (or
		 * equivalently, if this set changed as a result of the call). This set
		 * will not contain the arc once the call returns. The arc is just
		 * removed from the set itself. The linkage in the graph will not be
		 * manipulated.
		 * @param element The arc to be removed from this set, if present.
		 * @return {@code true} if the set contained the specified arc, or
		 * {@code false} otherwise.
		 */
		public boolean removeInternal(Object element) {
			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

			Object elem;
			int offset = 1;
			while((elem = t[hash]) != null) { //Look for the arc.
				if(elem == element) { //This is the arc we seek. Will never be true for null input.
					//So don't unlink it in the graph.
					t[hash] = tombstone; //R.I.P.
					modCount++;
					size--;
					return true;
				}
				hash = (hash + offset++) & tMax;
			}
			return false; //Reached an empty spot without any hit. Not found.
		}

		/**
		 * This iterator iterates over the arcs of a hash set. The iterator is a
		 * minor modification of the original {@link HashSet.Itr}
		 * implementation. The only difference it that removing an element
		 * through this iterator will also unlink it from the vertex the set
		 * belongs to that the iterator belongs to. The iterator gives the
		 * elements of the set in no particular order. The actual order will be
		 * the reverse order of the vertices in the set as they are ordered in
		 * the internal hash table of the set.
		 * <p>Note that the iterator will search through all buckets of the hash
		 * table to search for the elements of the set. The time complexity of a
		 * complete iteration of the set scales with the capacity of the hash
		 * table plus the number of elements in the set. Also, the time between
		 * two consecutive calls to {@link Iterator#next()} may vary.</p>
		 * <p>The iterator is fail-fast, which means that if a structural
		 * modification is made to the hash set after this method call, the
		 * iterator will throw a {@link ConcurrentModificationException} when
		 * {@link #next()} or {@link #remove()} is called. This behaviour
		 * prevents nondeterministic or unexpected behaviour caused by the
		 * concurrent modification of the set while it is being iterated over.
		 * Rather than giving inconsequent results, it always fails to give a
		 * result.</p>
		 */
		protected class Itr extends HashSet<Arc<V,A>>.Itr {
			/**
			 * Removes from the set the last element returned by this iterator.
			 * This method can be called only once per call to {@link #next()}.
			 * <p>The arc is also unlinked from the vertex in the graph.</p>
			 * @throws ConcurrentModificationException The set was structurally
			 * modified between the constructing of this iterator and the
			 * calling of this method.
			 * @throws IllegalStateException The {@link #next()} method has not
			 * yet been called, or the {@code remove()} method has already been
			 * called after the last call to the {@code next()} method.
			 */
			@Override
			@SuppressWarnings("unchecked") //Caused by the casting of the element to Arc to remove it from the graph.
			public void remove() {
				if(last < 0) { //Nothing to remove.
					throw new IllegalStateException();
				}
				if(modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
				//Delete the element at 'last'.
				final Object elem = table[last];
				if(elem != null && elem != tombstone) {
					((Arc<V,A>)elem).removeFromSourceInternal(Vertex.this); //Remove the vertex from the arc's source. Safe cast, since the set has only Arcs.
					table[last] = tombstone; //R.I.P.
					size--;
					modCount++;
					expectedModCount = modCount; //This modification is expected since the iterator made it.
					last = -1; //Set this to negative so the next call doesn't try to remove it again.
				}
			}
		}
	}
}