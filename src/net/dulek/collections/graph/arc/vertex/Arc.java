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

import java.util.Iterator;
import java.util.Set;
import net.dulek.collections.IdentityHashSet;

/**
 * This represents an arc in a graph. An arc is a relation between vertices. It
 * connects the vertices with eachother. Arcs are directed for this
 * implementation, and thus have two sides to them: A source and a destination,
 * or, respectively, where the arc comes from and where it goes to.
 * <p>Undirected subclasses of the {@code Graph} interface may choose to present
 * the endpoints more ambiguously, but the methods required by this interface
 * must still be properly implemented to work on one side of the arc. That is,
 * the methods working on the source of the arc must always work on the other
 * side as the methods working on the destination of the arc. It is suggested to
 * use an implementation that refers to the same arc from both sides.</p>
 * <p>Depending on the implementation of the {@code Graph} abstract class, an
 * arc may have multiple vertices in its source or destination (a hyperarc), it
 * may have no vertices in its source or destination (a halfarc), and it may be
 * directed or not. For the latter property, if the arc is undirected, it must
 * behave as if there are two arcs: One connecting vertices {@code A} to
 * vertices {@code B}, and one connecting vertices {@code B} to vertices
 * {@code A}. If one vertex is modified, its corresponding opposite arc will be
 * modified as well.</p>
 * @param <V> The type of data stored in the vertices to which this arc
 * connects.
 * @param <A> The type of data stored in this arc.
 */
public abstract class Arc<V,A> implements net.dulek.collections.graph.arc.Arc<Vertex<V,A>,A> {
	/**
	 * The graph that this arc is a member of. This is mostly required for
	 * optimisation in order to know in advance how big the graph is. It is also
	 * required to copy the context of the arc when the arc is copied.
	 * <p>If this arc is not a member of a graph, this should be {@code null}.
	 * The arc can only be part of one graph at a time.</p>
	 */
	protected Graph<V,A> graph;

	/**
	 * The unique identifier of this arc. Note that this identifier is only
	 * unique among the other arcs of this graph.
	 */
	protected int uid;

	/**
	 * The label to store in this arc.
	 */
	private A label;

	/**
	 * Constructs a new arc. The arc will not be placed in the graph or
	 * connected to any vertices yet without additional commands.
	 */
	protected Arc() {
		uid = Graph.nextArcUID++;
	}

	/**
	 * Adds the specified vertex to the set of destination vertices to which
	 * this arc goes. The method returns whether the arc was modified by the
	 * method call. When a vertex is already in the destination of this arc, it
	 * will not be added again and the arc is left unmodified.
	 * <p>This is equivalent to calling
	 * {@code destinationEndpoints().add(vertex)}.</p>
	 * @param vertex The vertex to add to the destination of this arc.
	 * @return {@code true} if the arc was modified by adding the specified
	 * vertex to its destination, or {@code false} otherwise.
	 * @throws IllegalStateException Adding a vertex to the destination of this
	 * arc would result in a hyperarc and hyperarcs are not allowed in this
	 * graph.
	 */
	@Override
	public abstract boolean addToDestination(Vertex<V,A> vertex);

	/**
	 * Adds the specified vertex to the set of source vertices from which this
	 * arc comes. The method returns whether the arc was modified by the method
	 * call. When a vertex is already in the source of this arc, it will not be
	 * added again and the arc is left unmodified.
	 * <p>This is equivalent to calling {@code sourceEndpoints().add(vertex)}.
	 * </p>
	 * @param vertex The vertex to add to the source of this arc.
	 * @return {@code true} if the arc was modified by adding the specified
	 * vertex to its source, or {@code false} otherwise.
	 * @throws IllegalStateException Adding a vertex to the source of this arc
	 * would result in a hyperarc and hyperarcs are not allowed in this graph.
	 */
	@Override
	public abstract boolean addToSource(Vertex<V,A> vertex);

	@Override
	public Arc<V,A> clone() {
		//Note: Do NOT perform a deep clone!
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Returns a set of all vertices in this arc's destination (where this arc
	 * is 'going to'). If the arc is a hyperarc, the set may have more than one
	 * element. If the arc is a halfarc, the set may be empty.
	 * <p>Modifying the set will modify the graph correspondingly.</p>
	 * @return A set of all vertices in this arc's destination.
	 */
	@Override
	public abstract Set<Vertex<V,A>> destinationEndpoints();

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Returns the label stored in this arc.
	 * @return The label stored in this arc.
	 */
	@Override
	public A getLabel() {
		return label;
	}

	/**
	 * Returns a hash code for the arc. This method is supported for the benefit
	 * of hashtables, as well to function as a heuristic for isomorphism tests.
	 * <p>The hash code fulfils the general contract of
	 * {@link Object#hashCode()}: It will remain the same as long as the graph
	 * structure doesn't change (which is a very weak promise), and if two arcs
	 * are the same according to the {@link equals(Object)} method, then the
	 * hash code of these two arcs will be the same.</p>
	 * <p>This hash code implementation tries to include as much structural
	 * information about the graph and the arc's position in it as possible,
	 * while still remaining invariate to graph automorphisms. This
	 * implementation takes into account every vertex and arc in the graph,
	 * hashing their labels and their in-degree and out-degree. The in-degree
	 * and out-degree of the vertices and arcs are raised to the {@code k}th
	 * power where {@code k} equals the smallest distance of that element to the
	 * original arc in number of vertex/arc traversals. The labels of the arcs
	 * are multiplied by {@code 31^k} and the labels of the vertices are
	 * multiplied by {@code 127^k}. The sum of the combined results for all
	 * reachable vertices and arcs in the graph is then completely flipped (all
	 * bits are flipped), and then this procedure is repeated but this time the
	 * arcs are traversed in the other direction, possibly producing different
	 * values for {@code k} and possibly resulting in a different set of
	 * vertices and arcs being reachable. The combined sum is subtracted from
	 * the previous result, and then returned. This procedure ensures that two
	 * corresponding arcs in isomorphic graphs will get the same hash code.</p>
	 * <p>Note that this method possibly traverses the entire graph, and
	 * therefore has {@code O(n + m)} worst-case complexity. This is quite
	 * complex for a hash code method. Therefore, identity hashes should be used
	 * where applicable.</p>
	 * @return A hash code for this vertex.
	 */
	@Override
	public int hashCode() {
		if(graph == null) { //No graph, just the arc.
			if(label == null) {
				return -1; //Empty, loose arc.
			}
			return 31 * label.hashCode(); //Loose arc, is just the label then.
		}

		//First, perform a BFS in the forward direction.
		int result = 0;
		int layer = 0; //The distance to the starting vertex.
		Set<Arc<V,A>> todoArcs = new IdentityHashSet<>(1); //Arcs we still have to process in this layer.
		todoArcs.add(this);
		Set<Vertex<V,A>> todoVertices; //Vertices we still have to process in this layer.
		final Set<Arc<V,A>> doneArcs = new IdentityHashSet<>(graph.numArcs()); //Arcs we don't want to process again.
		final Set<Vertex<V,A>> doneVertices = new IdentityHashSet<>(graph.numVertices()); //Vertices we don't want to process again.
		while(!todoArcs.isEmpty()) {
			layer++;
			todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
			for(final Arc<V,A> arc : todoArcs) {
				result += (arc.label == null ? 0 : net.dulek.math.Math.power(31,layer) * arc.label.hashCode()) + net.dulek.math.Math.power(arc.destinationEndpoints().size(),layer) + net.dulek.math.Math.power(arc.sourceEndpoints().size(),layer);
				for(final Vertex<V,A> vertex : arc.destinationEndpoints()) { //Add all unexplored vertices to the todo list.
					if(!doneVertices.contains(vertex)) {
						todoVertices.add(vertex);
					}
				}
				doneArcs.add(arc); //Don't visit this arc ever again.
			}
			todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
			for(final Vertex<V,A> vertex : todoVertices) {
				result += (vertex.getLabel() == null ? 0 : net.dulek.math.Math.power(127,layer) * vertex.getLabel().hashCode()) + net.dulek.math.Math.power(vertex.outgoingArcs().size(),layer) + net.dulek.math.Math.power(vertex.incomingArcs().size(),layer);
				for(final Arc<V,A> arc : vertex.outgoingArcs()) { //Add all unexplored arcs to the todo list.
					if(!doneArcs.contains(arc)) {
						todoArcs.add(arc);
					}
				}
				doneVertices.add(vertex); //Don't visit this vertex ever again.
			}
		}

		result ^= -1; //Flip all bits.

		//Next, perform a BFS in the backward direction.
		layer = 0;
		todoArcs = new IdentityHashSet<>(1); //Reset this set to the initial arc.
		todoArcs.add(this);
		doneArcs.clear(); //Reset the done flags.
		doneVertices.clear();
		while(!todoArcs.isEmpty()) {
			layer++;
			todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
			for(final Arc<V,A> arc : todoArcs) {
				result -= (arc.label == null ? 0 : net.dulek.math.Math.power(31,layer) * arc.label.hashCode()) + net.dulek.math.Math.power(arc.destinationEndpoints().size(),layer) + net.dulek.math.Math.power(arc.sourceEndpoints().size(),layer);
				for(final Vertex<V,A> vertex : arc.sourceEndpoints()) { //Add all unexplored vertices to the todo list.
					if(!doneVertices.contains(vertex)) {
						todoVertices.add(vertex);
					}
				}
				doneArcs.add(arc); //Don't visit this arc ever again.
			}
			todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
			for(final Vertex<V,A> vertex : todoVertices) {
				result -= (vertex.getLabel() == null ? 0 : net.dulek.math.Math.power(127,layer) * vertex.getLabel().hashCode()) + net.dulek.math.Math.power(vertex.outgoingArcs().size(),layer) + net.dulek.math.Math.power(vertex.incomingArcs().size(),layer);
				for(final Arc<V,A> arc : vertex.incomingArcs()) { //Add all unexplored arcs to the todo list.
					if(!doneArcs.contains(arc)) {
						todoArcs.add(arc);
					}
				}
				doneVertices.add(vertex); //Don't visit this vertex ever again.
			}
		}

		return result;
	}

	/**
	 * Returns whether this is a directed arc. This arc is directed if there is
	 * no arc in the graph with the same source vertices as this arc's
	 * destination vertices and the same destination vertices as this arc's
	 * source vertices. In other words, there is no arc with the reverse
	 * direction.
	 * @return {@code true} if this arc is directed, or {@code false} otherwise.
	 */
	@Override
	public boolean isDirected() {
		if(sourceEndpoints().isEmpty()) { //Halfarcs are always directed.
			return true;
		}
		if(destinationEndpoints().isEmpty()) {
			return true;
		}
		//All vertices in the source must have an incoming arc that is the reverse of this arc.
		final Vertex<V,A> vertex = sourceEndpoints().iterator().next(); //Find any one vertex and check.
		for(final Arc<V,A> arc : vertex.incomingArcs()) { //For all arcs coming into this vertex, see if it's the reverse arc.
			if(arc.sourceEndpoints().equals(destinationEndpoints()) && arc.destinationEndpoints().equals(sourceEndpoints())) {
				return false;
			}
		}
		//None of these arcs was the reverse. Must be directed then.
		return true;
	}

	/**
	 * Returns whether this is a reflexive arc. An arc is reflexive if its
	 * source vertices are the same as its destination vertices.
	 * @return {@code true} if this arc is reflexive, or {@code false}
	 * otherwise.
	 */
	@Override
	public boolean isReflexive() {
		return sourceEndpoints().equals(destinationEndpoints());
	}

	/**
	 * Removes the specified vertex from the list of destination vertices to
	 * which this arc goes. The method returns whether the arc was modified by
	 * the method call. When a vertex was not present in the destination of this
	 * arc, it can not be removed and the arc is left unmodified.
	 * <p>This is equivalent to calling
	 * {@code destinationEndpoints().remove(vertex)}.</p>
	 * @param vertex The vertex to remove from the destination of this arc.
	 * @return {@code true} if the arc was modified by removing the specified
	 * vertex from its destination, or {@code false} otherwise.
	 * @throws IllegalStateException Removing the vertex from the destination of
	 * the arc would result in a halfarc and halfarcs are not allowed in this
	 * graph.
	 */
	@Override
	public abstract boolean removeFromDestination(Vertex<V,A> vertex);

	/**
	 * Removes the specified vertex from the list of source vertices from which
	 * this arc comes. The method returns whether the arc was modified by the
	 * method call. When a vertex was not present in the source of this arc, it
	 * can not be removed and the arc is left unmodified.
	 * <p>This is equivalent to calling
	 * {@code sourceEndpoints().remove(vertex)}.</p>
	 * @param vertex The vertex to remove from the source of this arc.
	 * @return {@code true} if the arc was modified by removing the specified
	 * vertex from its source, or {@code false} otherwise.
	 * @throws IllegalStateException Removing the vertex from the source of the
	 * arc would result in a halfarc and halfarcs are not allowed in this graph.
	 */
	@Override
	public abstract boolean removeFromSource(Vertex<V,A> vertex);

	/**
	 * Changes the label in this arc to the specified label. The previous label
	 * will be returned.
	 * @param label The new label for the arc.
	 * @return The label of the arc before this method was called.
	 */
	@Override
	public A setLabel(A label) {
		final A oldLabel = this.label;
		this.label = label;
		return oldLabel;
	}

	/**
	 * Returns a set of all vertices in this arc's source (where this arc is
	 * 'coming from'). If the arc is a hyperarc, the set may have more than one
	 * element. If the arc is a halfarc, the set may be empty.
	 * <p>Modifying the set will modify the graph correspondingly.</p>
	 * @return A set of all vertices in this arc's source.
	 */
	@Override
	public abstract Set<Vertex<V,A>> sourceEndpoints();

	/**
	 * Returns a {@code String} representation of the arc. The string must
	 * indicate that the object is an arc and must include an indication of
	 * which vertices are in the arc's source and which are in the arc's
	 * destination.
	 * <p>The string representation for an arc will be an indicator that it is
	 * an arc followed by a space and the unique identifier between brackets,
	 * followed by a colon and a space. Next follows between curly brackets a
	 * comma-separated list of the unique identifiers of all source vertices, an
	 * arrow with the arc's label on it, and then between curly brackets a
	 * comma-separated list of the unique identifiers of all destination
	 * vertices. The representation is thus of the following format:<br />
	 * <code>arc (arcUID): {vertUID,...,vertUID} --arcLabel-&gt;
	 * {vertUID,...,vertUID}</code></p>
	 * @return A string representation of the arc.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder((sourceEndpoints().size() << 2) + (destinationEndpoints().size() << 2) + 28); //Reserve 4 chars for every vertex, 4 brackets, 6 for the arrow, 8 for the 'arc (): ', 2 for the uid, and 8 for the value.
		result.append("arc (");
		result.append(Integer.toString(uid));
		result.append("): {");
		Iterator<? extends Vertex<V,A>> it = sourceEndpoints().iterator();
		if(it.hasNext()) { //Don't prepend the first vertex with a comma.
			result.append(Integer.toString(it.next().uid));
			while(it.hasNext()) { //Print all the rest with commas before them.
				result.append(',');
				result.append(Integer.toString(it.next().uid));
			}
		}
		result.append("} --"); //First half of the arrow.
		if(label == null) {
			result.append("null");
		} else {
			result.append(label.toString()); //Label in between.
		}
		result.append("-> {"); //Second half of the arrow.
		it = destinationEndpoints().iterator();
		if(it.hasNext()) { //Don't prepend the first vertex with a comma.
			result.append(Integer.toString(it.next().uid));
			while(it.hasNext()) { //Print all the rest with commas before them.
				result.append(',');
				result.append(Integer.toString(it.next().uid));
			}
		}
		result.append('}');
		return result.toString();
	}

	/**
	 * Adds the specified vertex to the list of destination vertices to which
	 * this arc goes. The vertex is only added to the list of destination
	 * vertices of this arc. Other elements of the graph are untouched. If the
	 * operation would cause the graph to become invalid, an
	 * {@code IllegalStateException} will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When a vertex is coupled to the arc, the arc needs to be coupled to the
	 * vertex as well but without trying to couple the vertex to the arc again.
	 * </p>
	 * @param vertex The vertex to add to the destination of this arc.
	 * @throws IllegalStateException Adding a vertex to the destination of this
	 * arc would cause the graph to become invalid.
	 */
	protected abstract void addToDestinationInternal(Vertex<V,A> vertex);

	/**
	 * Adds the specified vertex to the list of source vertices from which this
	 * arc comes. The vertex is only added to the list of source vertices of
	 * this arc. Other elements of the graph are untouched. If the operation
	 * would cause the graph to become invalid, an {@code IllegalStateException}
	 * will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When a vertex is coupled to the arc, the arc needs to be coupled to the
	 * vertex as well but without trying to couple the vertex to the arc again.
	 * </p>
	 * @param vertex The vertex to add to the source of this arc.
	 * @throws IllegalStateException Adding a vertex to the source of this arc
	 * would cause the graph to become invalid.
	 */
	protected abstract void addToSourceInternal(Vertex<V,A> vertex);

	/**
	 * Removes all of the vertices from the destination to which this arc goes.
	 * Only the list of destination vertices is cleared. Other elements of the
	 * graph are untouched. If the operation would cause the graph to become
	 * invalid, an {@code IllegalStateException} will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When all vertices are removed, all vertices of the arc need to be
	 * uncoupled from the arc as well without trying to uncouple the arc from
	 * the vertex again.</p>
	 * @throws IllegalStateException Clearing the destination of the arc would
	 * cause the graph to become invalid.
	 */
	protected abstract void clearDestinationInternal();

	/**
	 * Removes all of the vertices from the source from which this arc comes.
	 * Only the list of source vertices is cleared. Other elements of the graph
	 * are untouched. If the operation would cause the graph to become invalid,
	 * an {@code IllegalStateException} will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When all vertices are removed, all vertices of the arc need to be
	 * uncoupled from the arc as well without trying to uncouple the arc from
	 * the vertex again.</p>
	 * @throws IllegalStateException Clearing the source of the arc would cause
	 * the graph to become invalid.
	 */
	protected abstract void clearSourceInternal();

	/**
	 * Removes the specified vertex from the list of destination vertices to
	 * which this arc goes. The vertex is only removed from the list of
	 * destination vertices of this arc. Other elements of the graph are
	 * untouched. If the operation would cause the graph to become invalid, an
	 * {@code IllegalStateException} will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When a vertex is uncoupled from the arc, the arc needs to be uncoupled
	 * from the vertex as well but without trying to uncouple the vertex from
	 * the arc again.</p>
	 * @param vertex The vertex to remove from the destination of this arc.
	 * @throws IllegalStateException Removing the vertex from the destination of
	 * this arc would cause the graph to become invalid.
	 */
	protected abstract void removeFromDestinationInternal(Vertex<V,A> vertex);

	/**
	 * Removes this arc from the incoming and outgoing arcs of all vertices.
	 * This is a helper method. This will remove the arc from the set of
	 * incoming arcs of all vertices in its destination, and from the set of
	 * outgoing arcs of all vertices in its source.
	 * <p>The arc is not removed from the {@code arcs} set of the graph. For
	 * that, the {@link #removeFromSet()} method must be used.</p>
	 * @see #removeFromSet()
	 */
	protected void removeFromGraph() {
		for(final Vertex<V,A> vertex : sourceEndpoints()) { //In its source, remove the outgoing arc.
			vertex.removeFromOutgoingInternal(this);
		}
		for(final Vertex<V,A> vertex : destinationEndpoints()) { //In its destination, remove the incoming arc.
			vertex.removeFromIncomingInternal(this);
		}
	}

	/**
	 * Removes this arc from the {@link arcs()} set of the graph. This is a
	 * helper method. The value returned by {@link Graph#numArcs()} will drop by
	 * {@code 1}.
	 * <p>The arc is not unlinked from the vertices of the graph, so traversal
	 * to it is still possible. To unlink the arc, the
	 * {@link #removeFromGraph()} method must be used.</p>
	 */
	protected void removeFromSet() {
		if(graph != null) {
			graph.arcs.removeInternal(this);
			graph = null;
		}
	}

	/**
	 * Removes the specified vertex from the list of source vertices from which
	 * this arc comes. The vertex is only removed from the list of source
	 * vertices of this arc. Other elements of the graph are untouched. If the
	 * operation would cause the graph to become invalid, an
	 * {@code IllegalStateException} will be thrown.
	 * <p>This method is intended to be called by other elements of the graph.
	 * When a vertex is uncoupled from the arc, the arc needs to be uncoupled
	 * from the vertex as well but without trying to uncouple the vertex from
	 * the arc again.</p>
	 * @param vertex The vertex to remove from the source of this arc.
	 * @throws IllegalStateException Removing the vertex from the source of this
	 * arc would cause the graph to become invalid.
	 */
	protected abstract void removeFromSourceInternal(Vertex<V,A> vertex);
}