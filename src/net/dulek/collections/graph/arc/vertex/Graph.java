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

import java.util.Map.Entry;
import java.util.*;
import java.util.Map.Entry;
import net.dulek.collections.HashSet;
import net.dulek.collections.IdentityHashSet;

/**
 * This class represents a graph with explicit arcs and vertices. The graph
 * consists of a set of vertices interconnected by arcs. This abstract class
 * further specifies that the graph must have explicit arcs and vertices, and
 * that it must be possible to represent hypergraphs and multigraphs with this.
 * Furthermore, it requires that the graph has data (labels) on the vertices and
 * on the arcs.
 * <p>The graph has two generic types, {@code V} and {@code A}. These represent
 * the types of data stored in respectively the vertices and the arcs. If there
 * is no data on the arcs or vertices of a particular usage of the graph, that
 * type should be {@link Void}.</p>
 * <p>The graph provides methods to modify the graph by adding and removing
 * vertices, and by adding and removing arcs. It provides methods to access and
 * modify the data on vertices and arcs. It provides methods to traverse the
 * graph by listing the adjacent vertices of a vertex, listing the incident
 * arcs of a vertex, and listing the endpoints of an arc. It provides
 * {@link Set} views of all vertices and all arcs, and methods to list or to
 * remove arcs and vertices with a specified label. Furthermore, a few helper
 * methods are provided as shortcuts to common operations, such as testing if
 * two vertices are adjacent. Lastly, some methods are provided to test
 * properties of the graph. This way, it may determine whether the instance is
 * actually one of {@code Graph}'s subclasses or else may be converted to one.
 * </p>
 * <p>Graphs may put restrictions on the allowed structures. This could
 * invalidate some of the operations on the graph that modify it. For instance,
 * if a specific {@code Graph} subclass must implement a tree, then any new arc
 * between two leaves would create a loop. If this happens, the invalid
 * operation should throw an {@link IllegalStateException} and undo any changes
 * already made. If an operation would always be invalid, an alternative method
 * should be provided with the same functionality. This may occur for instance
 * if a graph must always be connected, since new vertices will by default have
 * no incoming or outgoing arcs. An exception to this guideline is the removing
 * of arcs once their linked vertices have been removed. When a vertex is
 * removed, and this causes some arc to drop to zero vertices on one side, or
 * zero vertices in total for halfarcs, then that arc must also automatically be
 * removed, throwing an {@link IllegalStateException} if removing that arc is
 * illegal.</p>
 * <p>Note that this graph implementation is not meant for security systems. The
 * graph has numerous security flaws that may give an attacker some hints on the
 * internal structure of the graph in the system. For instance, the
 * {@link HashSet}s used for the vertex and arc set views of the graphs will
 * return their elements in an iteration based on the memory location of the
 * elements, which gives a hint on the order in which the elements were created.
 * In a secure implementation, such iterations should be scrambled and response
 * times should be padded, before communicating a response to untrusted users.
 * Also, this implementation relies on the identity hash code of its arcs and
 * vertices. Requesting the identity hash code of arcs and vertices is reported
 * to influence the correctness of biased locking mechanisms.</p>
 * @author Ruben Dulek
 * @param <V> The type of data stored in the vertices of the graph.
 * @param <A> The type of data stored in the arcs of the graph.
 * @see Arc
 * @see Vertex
 * @see net.dulek.collections.graph.Graph
 * @see net.dulek.collections.graph.arc.Graph
 * @version 1.0
 */
public abstract class Graph<V,A> implements net.dulek.collections.graph.arc.Graph<Vertex<V,A>,A> {
	/**
	 * This set contains all arcs in the graph. It is returned by the
	 * {@link #arcs()} method. The implementation of the set is custom, as to
	 * implement the interdependency of the two objects. When the set is
	 * modified, the custom implementation will ensure that the graph is
	 * modified accordingly.
	 */
	protected final ArcSet arcs = new ArcSet();

	/**
	 * The unique identifier the next vertex will receive if it is created.
	 * Every time a vertex is created, it will take the value of this field and
	 * increment this field for the next vertex to be created.
	 */
	protected static int nextVertexUID;

	/**
	 * The unique identifier the next arc will receive if it is created. Every
	 * time an arc is created, it will take the value of this field and
	 * increment this field for the next arc to be created.
	 */
	protected static int nextArcUID;

	/**
	 * This set contains all vertices in the graph. It is returned by the
	 * {@link #vertices()} method. The implementation of the set is custom, as
	 * to implement the interdependency of the two objects. When the set is
	 * modified, the custom implementation will ensure that the graph is
	 * modified accordingly.
	 */
	protected final VertexSet vertices = new VertexSet();

	/**
	 * Creates a new arc that connects the vertices in {@code from} to the
	 * vertices in {@code to}. The label in the arc will be {@code null} after
	 * the arc is created. The newly added arc will be returned.
	 * <p>Some extentions of this class may allow arcs to be hyperarcs or
	 * halfarcs. Therefore, the endpoints of an arc may have zero vertices, or
	 * more than one. Extentions that have undirected edges instead of directed
	 * arcs should behave as if they create two arcs, one in each direction,
	 * though these arcs may have the same {@code Arc} instance to represent
	 * them.</p>
	 * @param from The vertices from which this arc comes. Since this arc may be
	 * a hyperarc or a halfarc, any number of vertices may be on every endpoint.
	 * @param to The vertices to which this arc goes. Since this arc may be a
	 * hyperarc or a halfarc, any number of vertices may be on every endpoint.
	 * @return The newly created arc.
	 * @throws IllegalStateException The {@code addArc(Collection,Collection)}
	 * operation would cause the graph to become invalid.
	 * @throws NullPointerException At least one of the specified collections of
	 * vertices is {@code null}.
	 */
	@Override
	public abstract Arc<V,A> addArc(Collection<Vertex<V,A>> from,Collection<Vertex<V,A>> to);

	/**
	 * Creates a new arc that connects the vertices in {@code from} to the
	 * vertices in {@code to}. The specified label will be stored in the arc.
	 * The newly added arc will be returned.
	 * <p>Some extentions of this class may allow arcs to be hyperarcs or
	 * halfarcs. Therefore, the endpoints of an arc may have zero vertices, or
	 * more than one. Extentions that have undirected edges instead of directed
	 * arcs should behave as if they create two arcs, one in each direction,
	 * though these arcs may have the same {@code Arc} instance to represent
	 * them.</p>
	 * @param from The vertices from which this arc comes. Since this arc may be
	 * a hyperarc or a halfarc, any number of vertices may be on every endpoint.
	 * @param to The vertices to which this arc goes. Since this arc may be a
	 * hyperarc or a halfarc, any number of vertices may be on every endpoint.
	 * @param label The label to store in the new arc.
	 * @return The newly created arc that contains the specified label.
	 * @throws IllegalStateException The
	 * {@code addArc(Collection,Collection,A)} operation would cause the graph
	 * to become invalid.
	 */
	@Override
	public abstract Arc<V,A> addArc(Collection<Vertex<V,A>> from,Collection<Vertex<V,A>> to,A label);

	/**
	 * Creates a new vertex and adds it to the graph. The vertex will not be
	 * connected to any other vertices, but it will be a member of the graph and
	 * thus will be included in the {@link Set} view of the vertices. The label
	 * of the vertex will be {@code null} after it is created. The newly added
	 * vertex will be returned.
	 * @return The newly created vertex.
	 * @throws IllegalStateException The {@code addVertex()} operation would
	 * cause the graph to become invalid.
	 */
	@Override
	public abstract Vertex<V,A> addVertex();

	/**
	 * Creates a new vertex and adds it to the graph. The vertex will not be
	 * connected to any other vertices, but it will be a member of the graph and
	 * thus will be included in the {@link Set} view of the vertices. The
	 * specified label will be stored in the vertex.
	 * @param label The label to store in the new vertex.
	 * @return The newly created vertex.
	 * @throws IllegalStateException The {@code addVertex(V)} operation would
	 * cause the graph to become invalid.
	 */
	public abstract Vertex<V,A> addVertex(V label);

	/**
	 * Gives a set of all vertices adjacent to the specified vertex. A vertex is
	 * adjacent to the specified vertex if and only if there exists an arc with
	 * {@code vertex} in its source and the other vertex in its destination.
	 * Note that this relation may not be symmetric if the graph is directed.
	 * The vertices are returned in no specified order.
	 * <p>The resulting set will be a new, separate set of vertices. The graph
	 * will maintain no reference to the resulting set, nor vice-versa, so
	 * modifying the resulting set will have no influence on the graph or
	 * vice-versa.</p>
	 * @param vertex The vertex of which all adjacent vertices must be listed.
	 * @return The adjacent vertices of the specified vertex.
	 * @throws NullPointerException The specified vertex is {@code null}.
	 */
	@Override
	public Set<? extends Vertex<V,A>> adjacentVertices(final Vertex<V,A> vertex) {
		return vertex.adjacentVertices(); //Method call throws NullPointerException by itself if null.
	}

	/**
	 * Provides a set-view of the labels on all the arcs in the graph. The set
	 * is <strong>not</strong> backed by the graph. The set is generated at the
	 * moment this method is called and changes in the set are not reflected in
	 * the graph. More importantly, changes in the graph are not reflected in
	 * the set, so the graph does not need to re-compute whether a label is
	 * still in the graph after removing the label, to keep this set up-to-date.
	 * The set has no particular order (unless an implementation of the
	 * {@code Graph} further specifies it).
	 * @return A set of the labels of all arcs in the graph.
	 */
	@Override
	public Set<A> arcLabels() {
		final Set<A> result = new HashSet<>(arcs.size()); //In the worst case, all arcs have a different label. This might be dumb memory-wise, but speedwise it doesn't make much difference.
		for(final Arc<V,A> arc : arcs) {
			result.add(arc.getLabel()); //Set automatically removes duplicates.
		}
		return result;
	}

	/**
	 * Provides a set-view of the arcs in the graph. The set is backed by the
	 * graph, so changes to the graph are reflected in the set and vice-versa.
	 * If the graph or the set is modified while an iteration over the set is in
	 * progress, the result is unspecified. The set has no particular order
	 * (unless an extention of the {@code Graph} further specifies it).
	 * @return A set of all arcs in the graph.
	 */
	@Override
	public Set<? extends Arc<V,A>> arcs() {
		return arcs;
	}

	/**
	 * Provides a set-view of the arcs in the graph with the specified label.
	 * The set is <strong>not</strong> backed by the graph. The set is generated
	 * at the moment this method is called and changes in the set are not
	 * reflected in the graph. More importantly, changes in the graph are not
	 * reflected in the set, so the graph does not need to maintain a reference
	 * to all sets of arcs with every label in the graph, and change the sets
	 * every time a label is changed. The set has no particular order (unless a
	 * subclass of the {@code Graph} further specifies it).
	 * @param label The label of the arcs to provide a set of.
	 * @return A set of all arcs with the specified label.
	 */
	@Override
	public Set<? extends Arc<V,A>> arcs(final A label) {
		final Set<Arc<V,A>> result = new IdentityHashSet<>();
		if(label == null) { //If outside of loop for speed.
			for(final Arc<V,A> arc : arcs) { //Add arcs one by one.
				if(arc.getLabel() == null) { //Matches specified label.
					result.add(arc);
				}
			}
			return result;
		}
		for(final Arc<V,A> arc : arcs) { //Add arcs one by one.
			if(label.equals(arc.getLabel())) { //Matches specified label.
				result.add(arc);
			}
		}
		return result;
	}

	/**
	 * Removes all arcs and vertices from the graph, reverting it to its initial
	 * state as it was at the time of its creation.
	 * @throws IllegalStateException The {@code clear()} operation would cause
	 * the graph to become invalid.
	 */
	@Override
	public void clear() {
		for(final Vertex<V,A> vertex : vertices) {
			vertex.removeFromGraph(); //Unlink every vertex.
		}
		vertices.clearInternal(); //Let go of all vertices.
		for(final Arc<V,A> arc : arcs) {
			arc.removeFromGraph(); //Unlink every arc.
		}
		arcs.clearInternal(); //Let go of all arcs.
	}

	/**
	 * Creates and returns a copy of this graph. The resulting graph will equal
	 * this graph, despite being a different instance of it. The resulting graph
	 * will be a "deep copy", meaning that the resulting copy will maintain no
	 * references to any of the elements in this graph. Modifying the copy can
	 * in no way affect the original.
	 * @return A clone of this graph.
	 * @throws CloneNotSupportedException The implementation of the graph does
	 * not support cloning.
	 */
	@Override
	public abstract Graph<V,A> clone() throws CloneNotSupportedException;

	/**
	 * Lists the vertices in the destination of the specified arc. These are the
	 * vertices the arc is going towards. The vertices are listed in no
	 * specified order.
	 * <p>The set is backed by the graph, so changes to the set are reflected in
	 * the graph and vice-versa.</p>
	 * @param arc The arc to get the destination endpoints of.
	 * @return The destination endpoints of the specified arc.
	 * @throws NullPointerException The specified arc to get the destination
	 * endpoints of is {@code null}.
	 */
	@Override
	public Set<? extends Vertex<V,A>> destinationEndpoints(final net.dulek.collections.graph.arc.Arc<Vertex<V,A>,A> arc) {
		return arc.destinationEndpoints(); //Method call throws NullPointerException by itself if null.
	}

	@Override
	public Set<? extends Arc<V,A>> disconnect(final Vertex<V,A> from,final Vertex<V,A> to) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Tests whether some other object is a graph equal to this one. An object
	 * is equal to this graph if and only if:
	 * <ul><li>The object is not {@code null}.</li>
	 * <li>The object is an instance of {@code Graph}.</li>
	 * <li>The graphs are strongly isomorphic.</li>
	 * <li>The labels on equivalent vertices are equal.</li>
	 * <li>The labels on equivalent arcs are equal.</li></ul>
	 * These properties make the {@code equals(Object)} method reflexive,
	 * symmetric, transitive, consistent and makes {@code this.equals(null)}
	 * return {@code false}, as required by {@link Object#equals(Object)}.
	 * <p>The method requires the computation of whether the two graphs are
	 * isomorph. This is a computationally expensive problem for the general
	 * case, since no polynomial-time algorithm is yet known. The
	 * {@link #hashCode()} method should be used to approximate true graph
	 * equivalence, or the {@code equals(Object)} method should be used only on
	 * small graphs.</p>
	 * @param obj The object with which to compare.
	 * @return {@code true} if this graph is equal to the specified object, or
	 * {@code false} otherwise.
	 */
	@Override
	@SuppressWarnings({"unchecked","element-type-mismatch"})
	public boolean equals(Object obj) {
		if(obj == null) { //The object may not be null.
			return false;
		}
		if(!(obj instanceof Graph)) { //Check whether obj is an instance of Graph, as required.
			return false;
		}
		final Graph<Object,Object> graph = (Graph)obj; //Casting to Object will always succeed. We're comparing equality of labels with .equals(), so they may not be V and A.

		//That was the easy part. Now for directed hypergraph isomorphism!
		final int numVerticesMe = numVertices();
		final int numVerticesOther = graph.numVertices();
		final int numArcsMe = numArcs();
		final int numArcsOther = graph.numArcs();
		if(numVerticesMe != numVerticesOther || numArcsMe != numArcsOther) {
			return false; //If the number of arcs (or vertices) is not the same, it's not isomorph anyways.
		}
		if(numVerticesMe == 0) { //If there are no vertices, there are no arcs either.
			return true; //Both graphs are empty.
		}

		//As heuristic, we will hash all vertices and arcs, using an automorphism-invariant hash, then see if these hashes are identical.
		//These hashes require the hash of the vertex and arc labels often, so we'll cache these first.
		final Map<V,Integer> vertexLabelHashesMe = new IdentityHashMap<>(numVerticesMe);
		final Map<A,Integer> arcLabelHashesMe = new IdentityHashMap<>(numArcsMe);
		final Map<Object,Integer> vertexLabelHashesOther = new IdentityHashMap<>(numVerticesOther);
		final Map<Object,Integer> arcLabelHashesOther = new IdentityHashMap<>(numArcsOther);
		for(final Vertex<V,A> vertex : vertices) { //Compute the hash for all vertex labels.
			final V label = vertex.getLabel();
			if(!vertexLabelHashesMe.containsKey(label)) {
				vertexLabelHashesMe.put(label,label == null ? 0 : label.hashCode());
			}
		}
		for(final Arc<V,A> arc : arcs) { //Compute the local hash for all arcs.
			final A label = arc.getLabel();
			if(!arcLabelHashesMe.containsKey(label)) {
				arcLabelHashesMe.put(label,label == null ? 0 : label.hashCode());
			}
		}
		for(final Vertex<Object,Object> vertex : graph.vertices) {
			final Object label = vertex.getLabel();
			if(!vertexLabelHashesOther.containsKey(label)) {
				vertexLabelHashesOther.put(label,label == null ? 0 : label.hashCode());
			}
		}
		for(final Arc<Object,Object> arc : graph.arcs) {
			final Object label = arc.getLabel();
			if(!arcLabelHashesOther.containsKey(label)) {
				arcLabelHashesOther.put(label,label == null ? 0 : label.hashCode());
			}
		}

		//Next, compute the actual hashes and divide the vertices and arcs into equivalence classes based on that hash.
		//This graph's vertices.
		final Map<Long,Set<Vertex<V,A>>> vertexClassesMe = new HashMap<>(numVerticesMe);
		for(final Vertex<V,A> vertex : vertices) { //Compute the actual hash for all vertices.
			long hash = 0;
			int layer = 0;
			Set<Vertex<V,A>> todoVertices = new IdentityHashSet<>(1); //Vertices we still have to process in this layer.
			todoVertices.add(vertex);
			Set<Arc<V,A>> todoArcs; //Arcs we still have to process in this layer.
			final Set<Vertex<V,A>> doneVertices = new IdentityHashSet<>(numVerticesMe); //Vertices we don't want to process again.
			final Set<Arc<V,A>> doneArcs = new IdentityHashSet<>(numArcsMe); //Arcs we don't want to process again.
			while(!todoVertices.isEmpty()) {
				layer++;
				todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
				for(final Vertex<V,A> vert : todoVertices) {
					hash += vertexLabelHashesMe.get(vert.getLabel()) * net.dulek.math.Math.power(31,layer) + (net.dulek.math.Math.power((long)vert.outgoingArcs().size(),layer) << 32) + (net.dulek.math.Math.power((long)vert.incomingArcs().size(),layer) << 48);
					for(final Arc<V,A> arc : vert.outgoingArcs()) { //Add all unexplored arcs to the todo list.
						if(!doneArcs.contains(arc)) {
							todoArcs.add(arc);
						}
					}
					doneVertices.add(vert); //Don't visit this vertex ever again.
				}
				todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
				for(final Arc<V,A> arc : todoArcs) {
					hash += arcLabelHashesMe.get(arc.getLabel()) * net.dulek.math.Math.power(127,layer) + (net.dulek.math.Math.power((long)arc.sourceEndpoints().size(),layer) << 32) + (net.dulek.math.Math.power((long)arc.destinationEndpoints().size(),layer) << 48);
					for(final Vertex<V,A> vert : arc.destinationEndpoints()) { //Add all unexplored arcs to the todo list.
						if(!doneVertices.contains(vert)) {
							todoVertices.add(vert);
						}
					}
					doneArcs.add(arc); //Don't visit this arc ever again.
				}
			}

			hash ^= -1; //Flip all bits.

			//Next, perform a BFS in the backward direction.
			layer = 0;
			todoVertices = new IdentityHashSet<>(1);
			todoVertices.add(vertex);
			doneVertices.clear(); //Reset the done flags.
			doneArcs.clear();
			while(!todoVertices.isEmpty()) {
				layer++;
				todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
				for(final Vertex<V,A> vert : todoVertices) {
					hash -= vertexLabelHashesMe.get(vert.getLabel()) * net.dulek.math.Math.power(31,layer) + (net.dulek.math.Math.power((long)vert.outgoingArcs().size(),layer) << 32) + (net.dulek.math.Math.power((long)vert.incomingArcs().size(),layer) << 48);
					for(final Arc<V,A> arc : vert.incomingArcs()) { //Add all unexplored arcs to the todo list.
						if(!doneArcs.contains(arc)) {
							todoArcs.add(arc);
						}
					}
					doneVertices.add(vert); //Don't visit this vertex ever again.
				}
				todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
				for(final Arc<V,A> arc : todoArcs) {
					hash -= arcLabelHashesMe.get(arc.getLabel()) * net.dulek.math.Math.power(127,layer) + (net.dulek.math.Math.power((long)arc.sourceEndpoints().size(),layer) << 32) + (net.dulek.math.Math.power((long)arc.destinationEndpoints().size(),layer) << 48);
					for(final Vertex<V,A> vert : arc.sourceEndpoints()) { //Add all unexplored arcs to the todo list.
						if(!doneVertices.contains(vert)) {
							todoVertices.add(vert);
						}
					}
					doneArcs.add(arc); //Don't visit this arc ever again.
				}
			}

			if(!vertexClassesMe.containsKey(hash)) { //Haven't seen this hash yet!
				Set<Vertex<V,A>> equivalenceClass = new IdentityHashSet<>(1);
				equivalenceClass.add(vertex);
				vertexClassesMe.put(hash,equivalenceClass);
			} else { //This hash has been seen before. Add it to that class then.
				vertexClassesMe.get(hash).add(vertex);
			}
		}

		//The other graph's vertices.
		final Map<Long,Set<Vertex<Object,Object>>> vertexClassesOther = new HashMap<>(numVerticesOther);
		for(final Vertex<Object,Object> vertex : graph.vertices) { //Compute the actual hash for all vertices.
			long hash = 0;
			int layer = 0;
			Set<Vertex<Object,Object>> todoVertices = new IdentityHashSet<>(1); //Vertices we still have to process in this layer.
			todoVertices.add(vertex);
			Set<Arc<Object,Object>> todoArcs; //Arcs we still have to process in this layer.
			final Set<Vertex<Object,Object>> doneVertices = new IdentityHashSet<>(numVerticesOther); //Vertices we don't want to process again.
			final Set<Arc<Object,Object>> doneArcs = new IdentityHashSet<>(numArcsOther); //Arcs we don't want to process again.
			while(!todoVertices.isEmpty()) {
				layer++;
				todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
				for(final Vertex<Object,Object> vert : todoVertices) {
					hash += vertexLabelHashesOther.get(vert.getLabel()) * net.dulek.math.Math.power(31,layer) + (net.dulek.math.Math.power((long)vert.outgoingArcs().size(),layer) << 32) + (net.dulek.math.Math.power((long)vert.incomingArcs().size(),layer) << 48);
					for(final Arc<Object,Object> arc : vert.outgoingArcs()) { //Add all unexplored arcs to the todo list.
						if(!doneArcs.contains(arc)) {
							todoArcs.add(arc);
						}
					}
					doneVertices.add(vert); //Don't visit this vertex ever again.
				}
				todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
				for(final Arc<Object,Object> arc : todoArcs) {
					hash += arcLabelHashesOther.get(arc.getLabel()) * net.dulek.math.Math.power(127,layer) + (net.dulek.math.Math.power((long)arc.sourceEndpoints().size(),layer) << 32) + (net.dulek.math.Math.power((long)arc.destinationEndpoints().size(),layer) << 48);
					for(final Vertex<Object,Object> vert : arc.destinationEndpoints()) { //Add all unexplored vertices to the todo list.
						if(!doneVertices.contains(vert)) {
							todoVertices.add(vert);
						}
					}
					doneArcs.add(arc); //Don't visit this arc ever again.
				}
			}

			hash ^= -1; //Flip all bits.

			//Next, perform a BFS in the backward direction.
			layer = 0;
			todoVertices = new IdentityHashSet<>(1);
			todoVertices.add(vertex);
			doneVertices.clear(); //Reset the done flags.
			doneArcs.clear();
			while(!todoVertices.isEmpty()) {
				layer++;
				todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
				for(final Vertex<Object,Object> vert : todoVertices) {
					hash -= vertexLabelHashesOther.get(vert.getLabel()) * net.dulek.math.Math.power(31,layer) + (net.dulek.math.Math.power((long)vert.outgoingArcs().size(),layer) << 32) + (net.dulek.math.Math.power((long)vert.incomingArcs().size(),layer) << 48);
					for(final Arc<Object,Object> arc : vert.incomingArcs()) { //Add all unexplored arcs to the todo list.
						if(!doneArcs.contains(arc)) {
							todoArcs.add(arc);
						}
					}
					doneVertices.add(vert); //Don't visit this vertex ever again.
				}
				todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
				for(final Arc<Object,Object> arc : todoArcs) {
					hash -= arcLabelHashesOther.get(arc.getLabel()) * net.dulek.math.Math.power(127,layer) + (net.dulek.math.Math.power((long)arc.sourceEndpoints().size(),layer) << 32) + (net.dulek.math.Math.power((long)arc.destinationEndpoints().size(),layer) << 48);
					for(final Vertex<Object,Object> vert : arc.sourceEndpoints()) { //Add all unexplored vertices to the todo list.
						if(!doneVertices.contains(vert)) {
							todoVertices.add(vert);
						}
					}
					doneArcs.add(arc); //Don't visit this arc ever again.
				}
			}

			if(!vertexClassesMe.containsKey(hash)) { //This hash is not in this graph.
				return false;
			}
			if(!vertexClassesOther.containsKey(hash)) { //Haven't seen this hash yet!
				Set<Vertex<Object,Object>> equivalenceClass = new IdentityHashSet<>(1);
				equivalenceClass.add(vertex);
				vertexClassesOther.put(hash,equivalenceClass);
			} else { //This hash has been seen before. Add it to that class then.
				vertexClassesOther.get(hash).add(vertex);
			}
		}

		//Intermezzo: Check whether the vertex classes are equal to each other.
		if(vertexClassesMe.size() != vertexClassesOther.size()) { //Unequal number of different hashes.
			return false;
		}
		final Map<Set<Vertex<V,A>>,Set<Vertex<Object,Object>>> vertexClassMatchings = new IdentityHashMap<>(vertexClassesMe.size()); //Maintain which classes match to which classes.
		//We already know that every hash of the other graph is in this graph (checked at the end of the previous section), but not vice-versa and not whether they are equal size.
		for(final Entry<Long,Set<Vertex<V,A>>> entry : vertexClassesMe.entrySet()) {
			final Long key = entry.getKey();
			final Set<Vertex<V,A>> value = entry.getValue();
			if(!vertexClassesOther.containsKey(key)) { //Hash of this graph is not in the other graph.
				return false;
			}
			final Set<Vertex<Object,Object>> otherClass = vertexClassesOther.get(key);
			if(otherClass.size() != value.size()) { //Equivalence classes have different size.
				return false;
			}
			vertexClassMatchings.put(value,otherClass); //Store this match.
		}

		//This graph's arcs.
		final Map<Long,Set<Arc<V,A>>> arcClassesMe = new HashMap<>(numArcsMe);
		for(final Arc<V,A> arc : arcs) { //Compute the actual hash for all arcs.
			long hash = 0;
			int layer = 0;
			Set<Arc<V,A>> todoArcs = new IdentityHashSet<>(1); //Arcs we still have to process in this layer.
			todoArcs.add(arc);
			Set<Vertex<V,A>> todoVertices; //Vertices we still have to process in this layer.
			final Set<Arc<V,A>> doneArcs = new IdentityHashSet<>(numArcsMe); //Arcs we don't want to process again.
			final Set<Vertex<V,A>> doneVertices = new IdentityHashSet<>(numVerticesMe); //Vertices we don't want to process again.
			while(!todoArcs.isEmpty()) {
				layer++;
				todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
				for(final Arc<V,A> arc2 : todoArcs) {
					hash += arcLabelHashesMe.get(arc2.getLabel()) * net.dulek.math.Math.power(31,layer) + (net.dulek.math.Math.power((long)arc2.destinationEndpoints().size(),layer) << 32) + (net.dulek.math.Math.power((long)arc2.sourceEndpoints().size(),layer) << 48);
					for(final Vertex<V,A> vertex : arc2.destinationEndpoints()) { //Add all unexplored vertices to the todo list.
						if(!doneVertices.contains(vertex)) {
							todoVertices.add(vertex);
						}
					}
					doneArcs.add(arc2); //Don't visit this arc ever again.
				}
				todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
				for(final Vertex<V,A> vertex : todoVertices) {
					hash += vertexLabelHashesMe.get(vertex.getLabel()) * net.dulek.math.Math.power(127,layer) + (net.dulek.math.Math.power((long)vertex.outgoingArcs().size(),layer) << 32) + (net.dulek.math.Math.power((long)vertex.incomingArcs().size(),layer) << 48);
					for(final Arc<V,A> arc2 : vertex.outgoingArcs()) { //Add all unexplored arcs to the todo list.
						if(!doneArcs.contains(arc2)) {
							todoArcs.add(arc2);
						}
					}
					doneVertices.add(vertex); //Don't visit this vertex ever again.
				}
			}

			hash ^= -1; //Flip all bits.

			//Next, perform a BFS in the backward direction.
			layer = 0;
			todoArcs = new IdentityHashSet<>(1);
			todoArcs.add(arc);
			doneArcs.clear(); //Reset the done flags.
			doneVertices.clear();
			while(!todoArcs.isEmpty()) {
				layer++;
				todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
				for(final Arc<V,A> arc2 : todoArcs) {
					hash -= arcLabelHashesMe.get(arc2.getLabel()) * net.dulek.math.Math.power(31,layer) + (net.dulek.math.Math.power((long)arc2.destinationEndpoints().size(),layer) << 32) + (net.dulek.math.Math.power((long)arc2.sourceEndpoints().size(),layer) << 48);
					for(final Vertex<V,A> vertex : arc2.sourceEndpoints()) { //Add all unexplored vertices to the todo list.
						if(!doneVertices.contains(vertex)) {
							todoVertices.add(vertex);
						}
					}
					doneArcs.add(arc2); //Don't visit this arc ever again.
				}
				todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
				for(final Vertex<V,A> vertex : todoVertices) {
					hash -= vertexLabelHashesMe.get(vertex.getLabel()) * net.dulek.math.Math.power(127,layer) + (net.dulek.math.Math.power((long)vertex.outgoingArcs().size(),layer) << 32) + (net.dulek.math.Math.power((long)vertex.incomingArcs().size(),layer) << 48);
					for(final Arc<V,A> arc2 : vertex.incomingArcs()) { //Add all unexplored vertices to the todo list.
						if(!doneArcs.contains(arc2)) {
							todoArcs.add(arc2);
						}
					}
					doneVertices.add(vertex); //Don't visit this vertex ever again.
				}
			}

			if(!arcClassesMe.containsKey(hash)) { //Haven't seen this hash yet!
				Set<Arc<V,A>> equivalenceClass = new IdentityHashSet<>(1);
				equivalenceClass.add(arc);
				arcClassesMe.put(hash,equivalenceClass);
			} else { //This hash has been seen before. Add it to that class then.
				arcClassesMe.get(hash).add(arc);
			}
		}

		//The other graph's arcs.
		final Map<Long,Set<Arc<Object,Object>>> arcClassesOther = new HashMap<>(numArcsOther);
		for(final Arc<Object,Object> arc : graph.arcs) { //Compute the actual hash for all arcs.
			long hash = 0;
			int layer = 0;
			Set<Arc<Object,Object>> todoArcs = new IdentityHashSet<>(1); //Arcs we still have to process in this layer.
			todoArcs.add(arc);
			Set<Vertex<Object,Object>> todoVertices; //Vertices we still have to process in this layer.
			final Set<Arc<Object,Object>> doneArcs = new IdentityHashSet<>(numArcsMe); //Arcs we don't want to process again.
			final Set<Vertex<Object,Object>> doneVertices = new IdentityHashSet<>(numVerticesMe); //Vertices we don't want to process again.
			while(!todoArcs.isEmpty()) {
				layer++;
				todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
				for(final Arc<Object,Object> arc2 : todoArcs) {
					hash += arcLabelHashesOther.get(arc2.getLabel()) * net.dulek.math.Math.power(31,layer) + (net.dulek.math.Math.power((long)arc2.destinationEndpoints().size(),layer) << 32) + (net.dulek.math.Math.power((long)arc2.sourceEndpoints().size(),layer) << 48);
					for(final Vertex<Object,Object> vertex : arc2.destinationEndpoints()) { //Add all unexplored vertices to the todo list.
						if(!doneVertices.contains(vertex)) {
							todoVertices.add(vertex);
						}
					}
					doneArcs.add(arc2); //Don't visit this arc ever again.
				}
				todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
				for(final Vertex<Object,Object> vertex : todoVertices) {
					hash += vertexLabelHashesOther.get(vertex.getLabel()) * net.dulek.math.Math.power(127,layer) + (net.dulek.math.Math.power((long)vertex.outgoingArcs().size(),layer) << 32) + (net.dulek.math.Math.power((long)vertex.incomingArcs().size(),layer) << 48);
					for(final Arc<Object,Object> arc2 : vertex.outgoingArcs()) { //Add all unexplored arcs to the todo list.
						if(!doneArcs.contains(arc2)) {
							todoArcs.add(arc2);
						}
					}
					doneVertices.add(vertex); //Don't visit this vertex ever again.
				}
			}

			hash ^= -1; //Flip all bits.

			//Next, perform a BFS in the backward direction.
			layer = 0;
			todoArcs = new IdentityHashSet<>(1);
			todoArcs.add(arc);
			doneArcs.clear(); //Reset the done flags.
			doneVertices.clear();
			while(!todoArcs.isEmpty()) {
				layer++;
				todoVertices = new IdentityHashSet<>(todoArcs.size()); //Prepare a set of vertices to traverse afterwards.
				for(final Arc<Object,Object> arc2 : todoArcs) {
					hash -= arcLabelHashesOther.get(arc2.getLabel()) * net.dulek.math.Math.power(31,layer) + (net.dulek.math.Math.power((long)arc2.destinationEndpoints().size(),layer) << 32) + (net.dulek.math.Math.power((long)arc2.sourceEndpoints().size(),layer) << 48);
					for(final Vertex<Object,Object> vertex : arc2.sourceEndpoints()) { //Add all unexplored vertices to the todo list.
						if(!doneVertices.contains(vertex)) {
							todoVertices.add(vertex);
						}
					}
					doneArcs.add(arc2); //Don't visit this arc ever again.
				}
				todoArcs = new IdentityHashSet<>(todoVertices.size()); //Prepare a set of arcs to traverse afterwards.
				for(final Vertex<Object,Object> vertex : todoVertices) {
					hash -= vertexLabelHashesOther.get(vertex.getLabel()) * net.dulek.math.Math.power(127,layer) + (net.dulek.math.Math.power((long)vertex.outgoingArcs().size(),layer) << 32) + (net.dulek.math.Math.power((long)vertex.incomingArcs().size(),layer) << 48);
					for(final Arc<Object,Object> arc2 : vertex.incomingArcs()) { //Add all unexplored vertices to the todo list.
						if(!doneArcs.contains(arc2)) {
							todoArcs.add(arc2);
						}
					}
					doneVertices.add(vertex); //Don't visit this vertex ever again.
				}
			}

			if(!arcClassesMe.containsKey(hash)) { //This hash is not in this graph.
				return false;
			}
			if(!arcClassesOther.containsKey(hash)) { //Haven't seen this hash yet!
				Set<Arc<Object,Object>> equivalenceClass = new IdentityHashSet<>(1);
				equivalenceClass.add(arc);
				arcClassesOther.put(hash,equivalenceClass);
			} else { //This hash has been seen before. Add it to that class then.
				arcClassesOther.get(hash).add(arc);
			}
		}

		//Check whether the arc classes are equal to each other.
		if(arcClassesMe.size() != arcClassesOther.size()) { //Unequal number of different hashes.
			return false;
		}
		final Map<Set<Arc<V,A>>,Set<Arc<Object,Object>>> arcClassMatchings = new IdentityHashMap<>(arcClassesMe.size()); //Maintain which classes match to which classes.
		//We already know that every hash of the other graph is in this graph (checked at the end of the previous section), but not vice-versa and not whether they are equal size.
		for(final Entry<Long,Set<Arc<V,A>>> entry : arcClassesMe.entrySet()) {
			final Long key = entry.getKey();
			final Set<Arc<V,A>> value = entry.getValue();
			if(!arcClassesOther.containsKey(key)) { //Hash of this graph is not in the other graph.
				return false;
			}
			final Set<Arc<Object,Object>> other = arcClassesOther.get(key);
			if(other.size() != value.size()) { //Equivalence classes have different size.
				return false;
			}
			arcClassMatchings.put(value,other); //Store this match.
		}

		//Preprocessing for VF2: Sort the classes by size and put everything in lists so we can easily iterate and backtrack.
		final Comparator<Set<?>> comparatorSetBySize = new Comparator<Set<?>>() {
			/**
			 * Compare two sets by their cardinality. Any two sets can be
			 * compared. If set {@code a} is larger than set {@code b}, a
			 * positive integer is returned. If set {@code a} is smaller than
			 * set {@code b}, a negative integer is returned. Otherwise,
			 * {@code 0} is returned.
			 * @param a The first set to be compared.
			 * @param b The second set to be compared.
			 * @return A positive number if {@code a.size() > b.size()}, a
			 * negative number if {@code a.size() < b.size()}, or zero if they
			 * have the same size.
			 * @throws NullPointerException Either of the specified sets is
			 * {@code null}.
			 */
			@Override
			public int compare(Set<?> a,Set<?> b) {
				return a.size() - b.size();
			}
		}; //Compares sets by their cardinality.
		final List<Set<Vertex<V,A>>> sortedVertexClassesMe = new ArrayList<>(vertexClassesMe.values()); //First sort the classes.
		sortedVertexClassesMe.sort(comparatorSetBySize);
		final List<List<Vertex<V,A>>> vertexClassesBySizeMe = new ArrayList<>(vertexClassesMe.size()); //Then convert to lists and make sure the Other vertexClasses are in order too.
		final List<List<Vertex<Object,Object>>> vertexClassesBySizeOther = new ArrayList<>(vertexClassesOther.size());
		for(final Set<Vertex<V,A>> equivalenceClass : sortedVertexClassesMe) {
			final List<Vertex<V,A>> equivalenceClassMe = new ArrayList<>(equivalenceClass);
			vertexClassesBySizeMe.add(equivalenceClassMe);
			final List<Vertex<Object,Object>> equivalenceClassOther = new ArrayList<>(vertexClassMatchings.get(equivalenceClass));
			vertexClassesBySizeOther.add(equivalenceClassOther);
		}
		final List<Set<Arc<V,A>>> sortedArcClassesMe = new ArrayList<>(arcClassesMe.values()); //First sort the classes.
		sortedArcClassesMe.sort(comparatorSetBySize);
		final List<List<Arc<V,A>>> arcClassesBySizeMe = new ArrayList<>(arcClassesMe.size()); //Then convert to lists and make sure the Other arcClasses are in order too.
		final List<List<Arc<Object,Object>>> arcClassesBySizeOther = new ArrayList<>(arcClassesOther.size());
		for(final Set<Arc<V,A>> equivalenceClass : sortedArcClassesMe) {
			final List<Arc<V,A>> equivalenceClassMe = new ArrayList<>(equivalenceClass);
			arcClassesBySizeMe.add(equivalenceClassMe);
			final List<Arc<Object,Object>> equivalenceClassOther = new ArrayList<>(arcClassMatchings.get(equivalenceClass));
			arcClassesBySizeOther.add(equivalenceClassOther);
		}

		//Finally, the VF2 algorithm will do the actual matching. This part takes exponential time.
		final Map<Vertex<V,A>,Vertex<Object,Object>> vertexMatching = new IdentityHashMap<>(numVerticesMe);
		final Map<Arc<V,A>,Arc<Object,Object>> arcMatching = new IdentityHashMap<>(numArcsMe);
		//TODO: Split by connected components?

		//Make a priority queue to determine which matches are up next for consideration.
		//At every iteration, take the first element of that queue and try it. Keep the queue in the callstack and recurse.

		/*//First heuristic: Divide the vertices in equivalence classes with equal label.
		Map<V,Set<Vertex<V,A>>> vertexLabelClassesMe = new HashMap<>(numVerticesMe); //Sets mapped to the same vertex label are equivalent.
		Map<Object,Set<Vertex<Object,Object>>> vertexLabelClassesOther = new HashMap<>(numVerticesOther);
		for(final Vertex<V,A> vertex : vertices) { //Label classes of this graph.
			final V label = vertex.getLabel();
			if(!vertexLabelClassesMe.containsKey(label)) { //Haven't seen this label yet. Create a new equivalence class.
				final Set<Vertex<V,A>> labelClass = new IdentityHashSet<>();
				labelClass.add(vertex); //And add this vertex, of course.
				vertexLabelClassesMe.put(label,labelClass);
			} else {
				vertexLabelClassesMe.get(label).add(vertex); //Already existing class. Add the vertex to it.
			}
		}
		for(final Vertex<Object,Object> vertex : graph.vertices()) { //Vertex label classes of the other graph.
			final Object label = vertex.getLabel();
			if(!vertexLabelClassesMe.containsKey(label)) { //This graph doesn't have a label of the other graph. They are not equivalent.
				return false;
			}
			if(!vertexLabelClassesOther.containsKey(label)) { //Haven't seen this label yet. Create a new equivalence class.
				final Set<Vertex<Object,Object>> labelClass = new IdentityHashSet<>();
				labelClass.add(vertex); //And add this vertex, of course.
				vertexLabelClassesOther.put(label,labelClass);
			} else {
				vertexLabelClassesOther.get(label).add(vertex); //Already existing class. Add the vertex to it.
			}
		}
		//Now check if every class is of the same size.
		if(vertexLabelClassesMe.size() != vertexLabelClassesOther.size()) {
			return false;
		}
		for(final Entry<Object,Set<Vertex<Object,Object>>> entry : vertexLabelClassesOther.entrySet()) {
			final Object key = entry.getKey();
			if(!vertexLabelClassesMe.containsKey(key)) { //A label of the other graph is not in this graph.
				return false;
			}
			if(vertexLabelClassesMe.get(key).size() != entry.getValue().size()) { //The classes have different sizes.
				return false;
			}
		}

		//Second heuristic: Divide the vertex label classes into equivalence classes of equal (in- and out-)degree.
		Map<Pair<V,Integer>,Set<Vertex<V,A>>> vertexDegreeClassesMe = new HashMap<>(numVerticesMe); //Sets mapped to the same label and degree are equivalent.
		Map<Pair<Object,Integer>,Set<Vertex<Object,Object>>> vertexDegreeClassesOther = new HashMap<>(numVerticesOther);
		for(final Entry<V,Set<Vertex<V,A>>> entry : vertexLabelClassesMe.entrySet()) { //Degree classes of this graph.
			final V label = entry.getKey();
			for(final Vertex<V,A> vertex : entry.getValue()) { //For every vertex in this equivalence class, record its degree.
				final Pair<V,Integer> key = new Pair<>(label,vertex.outgoingArcs().size() + vertex.incomingArcs().size()); //The new signature of this vertex.
				if(!vertexDegreeClassesMe.containsKey(key)) { //Haven't seen this degree yet with this label. Create a new equivalence class.
					final Set<Vertex<V,A>> degreeClass = new IdentityHashSet<>();
					degreeClass.add(vertex); //And add this vertex, of course.
					vertexDegreeClassesMe.put(key,degreeClass);
				} else {
					vertexDegreeClassesMe.get(key).add(vertex); //Already existing degree class. Add the vertex to it.
				}
			}
		}
		for(final Entry<Object,Set<Vertex<Object,Object>>> entry : vertexLabelClassesOther.entrySet()) { //Degree classes of the other graph.
			final Object label = entry.getKey();
			for(final Vertex<Object,Object> vertex : entry.getValue()) { //For every vertex in this equivalence class, record its degree.
				final Pair<Object,Integer> key = new Pair<>(label,vertex.outgoingArcs().size() + vertex.incomingArcs().size()); //The new signature of this vertex.
				if(!vertexDegreeClassesMe.containsKey(key)) { //This graph doesn't have a vertex with the same degree and label. They are not equivalent.
					return false;
				}
				if(!vertexDegreeClassesOther.containsKey(key)) { //Haven't seen this degree yet with this label. Create a new equivalence class.
					final Set<Vertex<Object,Object>> degreeClass = new IdentityHashSet<>();
					degreeClass.add(vertex); //And add this vertex, of course.
					vertexDegreeClassesOther.put(key,degreeClass);
				} else {
					vertexDegreeClassesOther.get(key).add(vertex); //Already existing degree class. Add the vertex to it.
				}
			}
		}
		//Now check if every degree class is of the same size.
		if(vertexDegreeClassesMe.size() != vertexDegreeClassesOther.size()) {
			return false;
		}
		for(final Entry<Pair<Object,Integer>,Set<Vertex<Object,Object>>> entry : vertexDegreeClassesOther.entrySet()) {
			final Pair<Object,Integer> key = entry.getKey();
			if(!vertexDegreeClassesMe.containsKey(key)) { //A label-degree pair of the other graph is not in this graph.
				return false;
			}
			if(vertexDegreeClassesMe.get(key).size() != entry.getValue().size()) { //The degree classes have different sizes.
				return false;
			}
		}

		//Third heuristic: Divide the arcs in equivalence classes with the same label.
		Map<A,Set<Arc<V,A>>> arcLabelClassesMe = new HashMap<>(numArcsMe); //Sets mapped to the same vertex label are equivalent.
		Map<Object,Set<Arc<Object,Object>>> arcLabelClassesOther = new HashMap<>(numArcsOther);
		for(final Arc<V,A> arc : arcs) { //Label classes of this graph.
			final A label = arc.getLabel();
			if(!arcLabelClassesMe.containsKey(label)) { //Haven't seen this label yet. Create a new equivalence class.
				final Set<Arc<V,A>> labelClass = new IdentityHashSet<>();
				labelClass.add(arc); //And add this arc, of course.
				arcLabelClassesMe.put(label,labelClass);
			} else {
				arcLabelClassesMe.get(label).add(arc); //Already existing class. Add the arc to it.
			}
		}
		for(final Arc<Object,Object> arc : graph.arcs()) { //Vertex label classes of the other graph.
			final Object label = arc.getLabel();
			if(!arcLabelClassesMe.containsKey(label)) { //This graph doesn't have a label of the other graph. They are not equivalent.
				return false;
			}
			if(!arcLabelClassesOther.containsKey(label)) { //Haven't seen this label yet. Create a new equivalence class.
				final Set<Arc<Object,Object>> labelClass = new IdentityHashSet<>();
				labelClass.add(arc); //And add this arc, of course.
				arcLabelClassesOther.put(label,labelClass);
			} else {
				arcLabelClassesOther.get(label).add(arc); //Already existing class. Add the arc to it.
			}
		}
		//Now check if every class is of the same size.
		if(arcLabelClassesMe.size() != arcLabelClassesOther.size()) {
			return false;
		}
		for(final Entry<Object,Set<Arc<Object,Object>>> entry : arcLabelClassesOther.entrySet()) {
			final Object key = entry.getKey();
			if(!arcLabelClassesMe.containsKey(key)) { //A label of the other graph is not in this graph.
				return false;
			}
			if(arcLabelClassesMe.get(key).size() != entry.getValue().size()) { //The classes have different sizes.
				return false;
			}
		}

		//To speed up the exact matching for the next phase, we will sort the vertex equivalence classes by their size. Classes with low size are matched first, to produce a smaller tree.
		final List<Set<Vertex<V,A>>> equivalenceClassesMeBySize = new ArrayList<>(vertexDegreeClassesMe.values());
		final Comparator<Set<Vertex<V,A>>> comparatorBySize = new Comparator<Set<Vertex<V,A>>>() { //A comparator that compares sets by their size.
			/**
			 * Compares its two arguments by their size for order. Returns a
			 * negative integer, zero, or a positive integer as {@code a} is
			 * respectively smaller than, of equal size, or larger than
			 * {@code b}.
			 * <p>This implementation returns {@code a.size() - b.size()}. This
			 * implementation ensures that {@code compare(b,a)} returns the
			 * inverted sign of {@code compare(a,b)} and that the relation is
			 * transitive. Note that a return value of {@code 0} does not imply
			 * that the sets are equal, only that they have equal cardinality.
			 * This comparator imposes orderings that are inconsistent with
			 * {@code equals}.</p>
			 * @param a The first set to be compared. The result will be
			 * positive if and only if this set is larger.
			 * @param b The second set to be compared. The result will be
			 * positive if and only if this set is smaller.
			 * @return A negative integer, zero, or a positive integer as
			 * {@code a} is respectively smaller than, of equal size, or larger
			 * than {@code b}.
			 */
			/*@Override
			public int compare(final Set<Vertex<V,A>> a,final Set<Vertex<V,A>> b) {
				return a.size() - b.size();
			}
		};
		Collections.sort(equivalenceClassesMeBySize,comparatorBySize); //Sorted by size.
		final List<Vertex<V,A>> verticesByClassSize = new ArrayList<>(numVerticesMe); //Vertices sorted by the size of the equivalence class they are in.
		for(final Set<Vertex<V,A>> equivalenceClass : equivalenceClassesMeBySize) { //Add all vertices of all classes in order by class.
			for(final Vertex<V,A> vertex : equivalenceClass) {
				verticesByClassSize.add(vertex);
			}
		}*/

		//Exact solution: Enumerate all mappings with VF2.
		/* This implementation maintains 4 collections of sets: 1 of equivalence
		 * classes of vertices, 1 of equivalence classes of arcs, 1 of matched
		 * vertices (singleton sets) and 1 of matched arcs (singleton sets). The
		 * idea is to explore the tree of possibilities with pseudo-recursive
		 * tree search. At every iteration of the search:
		 *  - Either a vertex or an arc from the equivalence classes is matched
		 *    to one of its equivalent elements.
		 *  - If the incident graph elements do not match up (not in the same
		 *    equivalence classes and not matched), we try to match it with
		 *    another element.
		 *     - If this happens for an element with all of its equivalent
		 *       elements in the class, we backtrack.
		 *  - If the elements do match up, we move the elements to the matched
		 *    vertices or the matched arcs, and recurse.
		 * For the recursion we need to maintain a call stack that holds
		 * iterators. The iterators store the location in the set where the
		 * algorithm was at that point in the recursion.
		 */
		//TODO //Implementeer hashcode voor vertices op dat het een isomorfisme-invariante hashcode geeft van de DepthFirstSearch-tree (met als elementen vertices EN arcs!) met die vertex als root.
		     //Implementeer hashcode voor arcs op dat het een isomorfisme-invariante hashcode geeft van de DepthFirstSearch-tree (met als elementen vertices EN arcs!) met die arc als root.
		     //Optioneel: Gebruik voor de equals-methode een variant van hashcode die long teruggeeft voor extra accuratie.
		     //Optioneel: Cache de hashcodes.
		     //Optioneel: Verzin iets slims om de hashcodes van elke vertex tegelijk efficiënter uit te rekenen. Ik weet niet of dit mogelijk is, gezien sub-zoekbomen niet een vert of arc mogen bezoeken die al bezocht is.
		     //Verander de bovenstaande heuristieken zodat ze de hashcode gebruiken i.p.v. de label/degree enzo.
		     //Nieuwe heuristiek: Verwijder alle multiarcs.
		//STRATEGIE //Maak een initiële colouring met de hash codes van vertices en arcs.
		          //Ga met een best-first-search langs alle non-singleton sets van die kleuring.
		          //Dit doe je door een priority-queue te maken van paren van equivalente sets. Hier neem je telkens het eerste element af.
		          //  Als het niet een singleton-set is, kies dan een vertex v uit de set (best-first? zoek uit welke het beste is om een set te shatteren!)
		/*final Map<Vertex<V,A>,Vertex<Object,Object>> equivalenceMapping = new IdentityHashMap<>(numVerticesMe); //The 'solution' of the isomorphism: A mapping of vertices from this graph to the other.
		final Map<Vertex<V,A>,List<Vertex<Object,Object>>> vertexPossibilities = new IdentityHashMap<>(numVerticesMe); //Keep track of the possibilities we haven't tried yet.
		for(final Vertex<V,A> vertex : verticesByClassSize) {
			vertexPossibilities.put(vertex,new ArrayList<>(vertexDegreeClassesOther.get(new Pair<>(vertex.getLabel(),vertex.outgoingArcs().size() + vertex.incomingArcs().size()))));
		}
		final Map<Arc<V,A>,List<Arc<Object,Object>>> arcPossibilities = new IdentityHashMap<>(numArcsMe);
		for(final Entry<A,Set<Arc<V,A>>> entry : arcLabelClassesMe.entrySet()) {
			arcPossibilities.put(null,null)
		}
		int pos = 0; //Position in the verticesByClassSize list. This functions as our call stack.
		while(pos < verticesByClassSize.size()) {
			final Vertex<V,A> vertex = verticesByClassSize.get(pos);
			final List<Vertex<Object,Object>> possibleMappings = vertexPossibilities.get(vertex); //What are our options to map this vertex?
			while(!possibleMappings.isEmpty()) { //Find a possible mapping that is valid.
				final Vertex<Object,Object> other = possibleMappings.remove(possibleMappings.size() - 1); //Pick any possible vertex.
				if(thisMappingIsValid) {
					equivalenceMapping.put(vertex,other); //Store this mapping.
					pos++; //Go deeper into recursion.
					break; //Note: We are not yet restoring or erasing possibleMappings. We might need to continue with it if this is a dead end.
				}
			}

			pos--; //Exhausted our possibilities. We must backtrack.
			if(pos < 0) { //No possibilities at all. No mapping is possible!
				return false;
			}
			possibleMappings.addAll(vertexDegreeClassesOther.get(new Pair<>(vertex.getLabel(),vertex.outgoingArcs().size() + vertex.incomingArcs().size()))); //Restore the possibilities.
		}*/

		/*final Deque<Vertex<V,A>> callStackMe = new ArrayDeque<>(numVerticesMe); //Call stack for the simulated recursion. Vertices in here have already been matched.
		final Deque<Vertex<Object,Object>> callStackOther = new ArrayDeque<>(numVerticesMe);
		final Set<Vertex<V,A>> matchedMe = new IdentityHashSet<>(vertices); //Vertices that have already been matched. We'll only test adjacency of vertices in these sets.
		final Set<Vertex<Object,Object>> matchedOther = new IdentityHashSet<>(graph.vertices);
		int pos = 0; //Position in the equivalenceClassesMeBySize list.
		/* TODO: Herschrijf dit zodat hij equivalenceClassesMeBySize vertex per vertex af gaat in een call stack.
		while(pos < equivalenceClassesMeBySize.size()) { //While not every vertex is matched.
			Iterator<Vertex<V,A>> iterator = equivalenceClassesMeBySize.get(pos).getValue().iterator(); //Find the first vertex that's not
			Vertex<V,A> vertex;
			while(matchedMe.contains(vertex = iterator.next())); //Skip all vertices that are already matched.
			if(vertex == null) { //No vertex left. All is matched.
				pos++;
				continue; //Try again in a new iteration of the while loop.
			}
		}*/

		return true;
	}

	/**
	 * Returns the label of the specified arc.
	 * @param arc The arc to get the label of.
	 * @return The label of the specified arc.
	 * @throws NullPointerException The specified arc to get the label of is
	 * {@code null}.
	 */
	@Override
	public A getLabel(final net.dulek.collections.graph.arc.Arc<Vertex<V,A>,A> arc) {
		return arc.getLabel(); //Method call throws NullPointerException by itself if null.
	}

	/**
	 * Returns the label of the specified vertex.
	 * @param vertex The vertex to get the label of.
	 * @return The label of the specified vertex.
	 * @throws NullPointerException The specified vertex to get the label of is
	 * {@code null}.
	 */
	public V getLabel(final Vertex<V,A> vertex) {
		return vertex.getLabel(); //Method call throws NullPointerException by itself if null.
	}

	@Override
	public boolean hasCycle() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Returns whether this graph has labels on its arcs. Labels are extra
	 * pieces of data that are unrelated to the graph's adjacency structures.
	 * This implementation specifies that the graph has labels on its arcs, so
	 * this method will always return {@code true}.
	 * @return Always returns {@code true}, since this graph has labelled arcs.
	 */
	@Override
	public boolean hasLabelledArcs() {
		return true;
	}

	/**
	 * Returns whether this graph has labels on its vertices. Labels are extra
	 * pieces of data that are unrelated to the graph's adjacency structures.
	 * This implementation specifies that the graph has labels on its vertices,
	 * so this method will always return {@code true}.
	 * @return Always returns {@code true}, since this graph has labelled
	 * vertices.
	 */
	@Override
	public boolean hasLabelledVertices() {
		return true;
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean hasNullOnArcs() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	public boolean hasNullOnVertices() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean hasReflexiveArcs() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Gives a set of all arcs coming into the specified vertex. An arc is
	 * coming into the vertex if it has the vertex in its destination. The
	 * incoming arcs are listed in no particular order.
	 * <p>The set is backed by the graph, so changes to the set are reflected in
	 * the graph and vice-versa.</p>
	 * @param vertex The vertex to list the incoming arcs of.
	 * @return The incoming arcs of the specified vertex.
	 * @throws NullPointerException The specified vertex to get the incoming
	 * arcs of is {@code null}.
	 */
	@Override
	public Set<? extends Arc<V,A>> incomingArcs(Vertex<V,A> vertex) {
		return vertex.incomingArcs(); //Method call throws NullPointerException by itself if null.
	}

	/**
	 * Returns whether or not the {@code other} vertex is adjacent to the
	 * {@code vertex}. The {@code other} vertex is adjacent to the specified
	 * {@code vertex} if and only if there exists an arc with {@code vertex} in
	 * its source and {@code other} in its destination. Note that this relation
	 * may not be symmetric if the graph is directed.
	 * @param vertex The viewpoint-vertex to which the {@code other} vertex may
	 * be adjacent.
	 * @param other The vertex that may be adjacent to {@code vertex}.
	 * @return {@code true} if {@code other} is adjacent to {@code vertex}, or
	 * {@code false} otherwise.
	 * @throws NullPointerException At least one of the specified vertices is
	 * {@code null}.
	 */
	@Override
	public boolean isAdjacent(final Vertex<V,A> vertex,final Vertex<V,A> other) {
		return vertex.isAdjacent(other); //Method throws NullPointerException by itself if either is null.
	}

	/**
	 * Returns whether or not there exists a path from {@code vertex} to
	 * {@code other}. A path is a list of arcs, where the first arc has
	 * {@code vertex} in its source and the last arc has {@code other} in its
	 * destination, and all other arcs in between have a vertex in its source
	 * that the previous arc has in its destination, and a vertex in its
	 * destination that the next arc has in its source. If the graph is
	 * undirected, this is equivalent to whether the two vertices are in the
	 * same connected component.
	 * @param vertex The origin of the path whose existence must be checked.
	 * @param other The destination of the path whose existence must be checked.
	 * @return {@code true} if a path exists from {@code vertex} to
	 * {@code other}, or {@code false} otherwise.
	 * @throws NullPointerException At least one of the specified vertices is
	 * {@code null}.
	 */
	@Override
	public boolean isConnected(final Vertex<V,A> vertex,final Vertex<V,A> other) {
		return vertex.canReach(other); //Method throws NullPointerException by itself if either is null.
	}

	@Override
	public boolean isDirected() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean isHalf() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean isHyper() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean isMulti() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean isSubgraphOf(net.dulek.collections.graph.Graph<Vertex<V,A>,net.dulek.collections.graph.arc.Arc<Vertex<V,A>,A>> other) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Gives the total number of arcs in the graph. This is equivalent to
	 * {@code arcs().size()}.
	 * @return The total number of arcs in the graph.
	 */
	@Override
	public int numArcs() {
		return arcs.size();
	}

	/**
	 * Gives the total number of vertices in the graph. This is equivalent to
	 * {@code vertices().size()}.
	 * @return The total number of vertices in the graph.
	 */
	@Override
	public int numVertices() {
		return vertices.size();
	}

	/**
	 * Gives a set of all arcs going out of the specified vertex. An arc is
	 * going out of the vertex if it has the vertex in its source. The outgoing
	 * arcs are listed in no specified order.
	 * <p>The set is backed by the graph, so changes to the set are reflected in
	 * the graph and vice-versa.</p>
	 * @param vertex The vertex to list the outgoing arcs of.
	 * @return The outgoing arcs of the specified vertex.
	 * @throws NullPointerException The specified vertex to get the outgoing
	 * arcs of is {@code null}.
	 */
	@Override
	public Set<? extends Arc<V,A>> outgoingArcs(final Vertex<V,A> vertex) {
		return vertex.outgoingArcs(); //Method call throws NullPointerException by itself if null.
	}

	/**
	 * Removes all arcs from the graph that are also contained in the specified
	 * arc collection. After this call returns, this graph will have no arcs in
	 * common with the specified collection of arcs. Arcs that are in the
	 * specified collection but not in the graph will be ignored.
	 * @param arcs A collection of arcs that must be removed from the graph.
	 * @return A set of all removed arcs. This may be a different set of arcs
	 * from the specified collection, since not all arcs of the collection may
	 * have been present in the graph.
	 * @throws IllegalStateException The {@code removeAllArcs(Collection)}
	 * operation would cause the graph to become invalid.
	 * @throws NullPointerException The specified arc collection is
	 * {@code null}.
	 */
	@Override
	public Set<? extends Arc<V,A>> removeAllArcs(final Collection<net.dulek.collections.graph.arc.Arc<Vertex<V,A>,A>> arcs) {
		//Sadly, ArcSet.removeAll(Collection) returns a boolean whether it removed any arcs, per the Collection interface it adheres to. Therefore we need to remove all arcs one by one, manually.
		if(arcs == null) {
			throw new NullPointerException("The specified collection to remove all arcs of is null.");
		}

		final int numArcs = numArcs(); //Cache these.
		final int arcsSize = arcs.size();
		final Set<Arc<V,A>> removed = new IdentityHashSet<>(arcsSize); //Allocate enough room to store all arcs in the arcs collection.
		//Pick whichever method is fastest:
		if(numArcs > arcsSize || (arcs instanceof List && numArcs > Math.sqrt(arcsSize))) { //Iterate over the collection, removing all of its elements from the arcs. Lists have linear contains() methods, so they get special treatment.
			for(final net.dulek.collections.graph.arc.Arc<Vertex<V,A>,A> arc : arcs) {
				final Arc<V,A> localArc; //Need to convert every arc, since the interface specifies graph.arc.Arc.
				try { //Try to cast this to graph.arc.vertex.Arc. If it fails, it can't be in the graph.
					localArc = (Arc<V,A>)arc;
				} catch(ClassCastException e) { //The arc couldn't be cast.
					continue; //This arc can't be in the graph.
				}
				if(localArc.graph == this) { //Only remove the arc if it's actually in the graph.
					try {
						localArc.removeFromGraph();
						localArc.removeFromSet();
						removed.add(localArc);
					} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(final Arc<V,A> arc2 : removed) {
							this.arcs.add(arc2);
							for(final Vertex<V,A> vertex : arc2.sourceEndpoints()) { //Re-connect the arc to its vertices.
								vertex.addToOutgoingInternal(arc2);
							}
							for(final Vertex<V,A> vertex : arc2.destinationEndpoints()) {
								vertex.addToIncomingInternal(arc2);
							}
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			return removed;
		}
		//Otherwise, iterate over the arc set, removing the arcs that are in arcs.
		for(final Arc<V,A> arc : this.arcs) {
			if(arcs.contains(arc)) {
				try {
					arc.removeFromGraph();
					arc.removeFromSet();
					removed.add(arc);
				} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
					for(final Arc<V,A> arc2 : removed) {
						this.arcs.add(arc2);
						for(final Vertex<V,A> vertex : arc2.sourceEndpoints()) { //Re-connect the arc to its vertices.
							vertex.addToOutgoingInternal(arc2);
						}
						for(final Vertex<V,A> vertex : arc2.destinationEndpoints()) {
							vertex.addToIncomingInternal(arc2);
						}
					}
					throw e; //Pass the original exception on.
				}
			}
		}
		return removed;
	}

	/**
	 * Removes all arcs with the specified label from the graph, and returns a
	 * set of all removed arcs. If no arcs with the specified label are found,
	 * an empty set will be returned.
	 * @param label The label of the arcs that are to be removed.
	 * @return The set of all removed arcs.
	 * @throws IllegalStateException The {@code removeAllArcsByLabel(A)}
	 * operation would cause the graph to become invalid.
	 */
	@Override
	public Set<? extends Arc<V,A>> removeAllArcsByLabel(final A label) {
		final Set<Arc<V,A>> removed = new IdentityHashSet<>();
		if(label == null) { //If outside of loop for speed.
			for(final Arc<V,A> arc : arcs) { //Remove arcs one by one.
				if(arc.getLabel() == null) { //Matches label to remove.
					try {
						arc.removeFromGraph();
						arc.removeFromSet();
						removed.add(arc);
					} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(final Arc<V,A> arc2 : removed) {
							arcs.add(arc2);
							arc2.graph = this;
							for(final Vertex<V,A> vertex : arc2.sourceEndpoints()) { //Re-connect the arc to its vertices.
								vertex.addToOutgoingInternal(arc2);
							}
							for(final Vertex<V,A> vertex : arc2.destinationEndpoints()) {
								vertex.addToIncomingInternal(arc2);
							}
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			return removed;
		}
		for(final Arc<V,A> arc : arcs) { //Remove arcs one by one.
			if(label.equals(arc.getLabel())) { //Matches label to remove.
				try {
					arc.removeFromGraph();
					arc.removeFromSet();
					removed.add(arc);
				} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
					for(final Arc<V,A> arc2 : removed) {
						arcs.add(arc2);
						for(final Vertex<V,A> vertex : arc2.sourceEndpoints()) { //Re-connect the arc to its vertices.
							vertex.addToOutgoingInternal(arc2);
						}
						for(final Vertex<V,A> vertex : arc2.destinationEndpoints()) {
							vertex.addToIncomingInternal(arc2);
						}
					}
					throw e; //Pass the original exception on.
				}
			}
		}
		return removed;
	}

	/**
	 * Removes all vertices from the graph that are also contained in the
	 * specified vertex collection. After this call returns, this graph will
	 * have no vertices in common with the specified collection of vertices.
	 * Vertices that are in the specified collection but not in the graph will
	 * be ignored.
	 * <p>If removing a vertex causes an arc to have too few connections on any
	 * side, this arc will also automatically be removed. For graphs where
	 * halfarcs are allowed, this is when removing the vertex causes the arc to
	 * have no vertices in its source and destination. For graphs where
	 * halfarcs are not allowed, this is when removing the vertex causes the arc
	 * to have no vertices in either its source or in its destination. If
	 * removing the arc causes the graph to become illegal, an
	 * {@link IllegalStateException} will also be thrown.</p>
	 * @param vertices A collection of vertices that must be removed from the
	 * graph.
	 * @return A set of all removed vertices. This may be a different set of
	 * vertices from the specified collection, since not all vertices of the
	 * collection may have been present in the graph.
	 * @throws IllegalStateException The {@code removeAllVertices(Collection)}
	 * operation would cause the graph to become invalid.
	 * @throws NullPointerException The specified vertex collection is
	 * {@code null}.
	 */
	@Override
	public Set<? extends Vertex<V,A>> removeAllVertices(final Collection<Vertex<V,A>> vertices) {
		//Sadly, VertexSet.removeAll(Collection) returns a boolean whether it removed any vertices, per the Collection interface it adheres to. Therefore we need to remove all vertices one by one, manually.
		if(vertices == null) {
			throw new NullPointerException("The specified collection to remove all vertices of is null.");
		}

		final int numVertices = numVertices(); //Cache these.
		final int verticesSize = vertices.size();
		final Set<Vertex<V,A>> removed = new IdentityHashSet<>(verticesSize); //Allocate enough room to store all vertices in the vertices collection.
		//Pick whichever method is fastest:
		if(numVertices > verticesSize || (vertices instanceof List && numVertices > Math.sqrt(verticesSize))) { //Iterate over the collection, removing all of its elements from the vertices. Lists have linear contains() methods, so they get special treatment.
			for(final Vertex<V,A> vertex : vertices) {
				if(vertex.graph == this) { //Only remove the vertex if it's actually in the graph.
					try {
						vertex.removeFromGraph();
						vertex.removeFromSet();
						removed.add(vertex);
					} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(final Vertex<V,A> vertex2 : removed) {
							this.vertices.add(vertex2);
							for(final Arc<V,A> arc : vertex2.outgoingArcs()) { //Re-connect the vertex to its arcs.
								arc.addToSourceInternal(vertex2);
							}
							for(final Arc<V,A> arc : vertex2.incomingArcs()) {
								arc.addToDestinationInternal(vertex2);
							}
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			return removed;
		}
		//Otherwise, iterate over the vertex set, removing the vertices that are in vertices.
		for(final Vertex<V,A> vertex : this.vertices) {
			if(vertices.contains(vertex)) {
				try {
					vertex.removeFromGraph();
					vertex.removeFromSet();
					removed.add(vertex);
				} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
					for(final Vertex<V,A> vertex2 : removed) {
						this.vertices.add(vertex2);
						for(final Arc<V,A> arc : vertex2.outgoingArcs()) { //Re-connect the vertex to its arcs.
							arc.addToSourceInternal(vertex2);
						}
						for(final Arc<V,A> arc : vertex2.incomingArcs()) {
							arc.addToDestinationInternal(vertex2);
						}
					}
					throw e; //Pass the original exception on.
				}
			}
		}
		return removed;
	}

	/**
	 * Removes all vertices with the specified label from the graph, and returns
	 * a set of all removed vertices. If no vertices with the specified value
	 * are found, an empty set will be returned.
	 * <p>If removing a vertex causes an arc to have too few connections on any
	 * side, this arc will also automatically be removed. For graphs where
	 * halfarcs are allowed, this is when removing the vertex causes the arc to
	 * have no vertices in its source and destination. For graphs where
	 * halfarcs are not allowed, this is when removing the vertex causes the arc
	 * to have no vertices in either its source or in its destination. If
	 * removing the arc causes the graph to become illegal, an
	 * {@link IllegalStateException} will also be thrown.</p>
	 * @param value The value of the vertices that are to be removed.
	 * @return A set of all removed vertices.
	 * @throws IllegalStateException The {@code removeAllVerticesByLabel(V)}
	 * operation would cause the graph to become invalid.
	 */
	public Set<? extends Vertex<V,A>> removeAllVerticesByLabel(final V value) {
		final Set<Vertex<V,A>> removed = new IdentityHashSet<>();
		if(value == null) { //If outside of loop for speed.
			for(final Vertex<V,A> vertex : vertices) { //Remove vertices one by one.
				if(vertex.getLabel() == null) { //Matches label to remove.
					try {
						vertex.removeFromGraph();
						vertex.removeFromSet();
						removed.add(vertex);
					} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(final Vertex<V,A> vertex2 : removed) {
							vertices.add(vertex2);
							vertex2.graph = this;
							for(final Arc<V,A> arc : vertex2.outgoingArcs()) { //Re-connect the vertex to its arcs.
								arc.addToSourceInternal(vertex2);
							}
							for(final Arc<V,A> arc : vertex2.incomingArcs()) {
								arc.addToDestinationInternal(vertex2);
							}
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			return removed;
		}
		for(final Vertex<V,A> vertex : vertices) { //Remove vertices one by one.
			if(value.equals(vertex.getLabel())) { //Matches label to remove.
				try {
					vertex.removeFromGraph();
					vertex.removeFromSet();
					removed.add(vertex);
				} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
					for(final Vertex<V,A> vertex2 : removed) {
						vertices.add(vertex2);
						for(final Arc<V,A> arc : vertex2.outgoingArcs()) { //Re-connect the vertex to its arcs.
							arc.addToSourceInternal(vertex2);
						}
						for(final Arc<V,A> arc : vertex2.incomingArcs()) {
							arc.addToDestinationInternal(vertex2);
						}
					}
					throw e; //Pass the original exception on.
				}
			}
		}
		return removed;
	}

	/**
	 * Removes the specified arc from the graph. The method returns whether the
	 * graph was modified by the method call. If the specified arc is not
	 * present in the graph, the graph will not be modified and {@code false}
	 * will be returned. Otherwise, the arc is removed and {@code true} will be
	 * returned.
	 * @param arc The arc that must be removed from the graph.
	 * @return {@code true} if the arc was present in the graph prior to
	 * removing, or {@code false} otherwise.
	 * @throws IllegalStateException The {@code removeArc(Arc)} operation
	 * would cause the graph to become invalid.
	 * @throws NullPointerException The specified arc is {@code null}.
	 */
	@Override
	public boolean removeArc(final net.dulek.collections.graph.arc.Arc<Vertex<V,A>,A> arc) {
		final Arc<V,A> localArc; //Need to convert the arc, since the interface specifies graph.arc.Arc.
		try { //Try to cast this to graph.arc.vertex.Arc. If it fails, it can't be in the graph.
			localArc = (Arc<V,A>)arc;
		} catch(ClassCastException e) { //The arc couldn't be cast.
			return false; //Then the arc can't be in the graph.
		}
		if(localArc.graph != this) { //The arc is not in the graph.
			return false;
		}
		localArc.removeFromGraph(); //This may throw an IllegalStateException.
		localArc.removeFromSet();
		return true;
	}

	/**
	 * Removes an arc with the specified label from the graph, and returns it.
	 * If no arc with the specified label is found, {@code null} will be
	 * returned.
	 * @param label The label of the arc that is to be removed.
	 * @return The removed arc, or {@code null} if no such arc was found.
	 * @throws IllegalStateException The {@code removeArcByLabel(A)} operation
	 * would cause the graph to become invalid.
	 */
	@Override
	public Arc<V,A> removeArcByLabel(final A label) {
		if(label == null) { //If outside loop for speed.
			for(final Arc<V,A> arc : arcs) { //Check each arc one by one.
				if(arc.getLabel() == null) { //This is the arc to remove.
					arc.removeFromGraph(); //This one may throw an IllegalStateException.
					arc.removeFromSet();
					return arc;
				}
			}
			return null; //No arc with the null-value was found.
		}
		for(final Arc<V,A> arc : arcs) { //Check each arc one by one.
			if(label.equals(arc.getLabel())) { //This is the arc to remove.
				arc.removeFromGraph(); //This one may throw an IllegalStateException.
				arc.removeFromSet();
				return arc;
			}
		}
		return null; //No arc with the specified label was found.
	}

	/**
	 * Removes a vertex with the specified label from the graph, and returns it.
	 * If no vertex with the specified label is found, {@code null} will be
	 * returned.
	 * <p>If removing a vertex causes an arc to have too few vertices on any
	 * side, this arc will also automatically be removed. For graphs where
	 * halfarcs are allowed, this is when removing the vertex causes the arc to
	 * have no vertices in its source and destination. For graphs where
	 * halfarcs are not allowed, this is when removing the vertex causes the arc
	 * to have no vertices in either its source or in its destination. If
	 * removing the arc causes the graph to become illegal, an
	 * {@link IllegalStateException} will be thrown.</p>
	 * @param label The label of the vertex that is to be removed.
	 * @return The removed vertex, or {@code null} if no such vertex was found.
	 * @throws IllegalStateException The {@code removeVertexByLabel(V)}
	 * operation would cause the graph to become invalid.
	 */
	public Vertex<V,A> removeVertexByLabel(final V label) {
		if(label == null) { //If outside loop for speed.
			for(final Vertex<V,A> vertex : vertices) { //Check each vertex one by one.
				if(vertex.getLabel() == null) { //This is the vertex to remove.
					vertex.removeFromGraph(); //This one may throw an IllegalStateException.
					vertex.removeFromSet();
					return vertex;
				}
			}
			return null; //No vertex with the null-value was found.
		}
		for(final Vertex<V,A> vertex : vertices) { //Check each vertex one by one.
			if(label.equals(vertex.getLabel())) { //This is the vertex to remove.
				vertex.removeFromGraph(); //This one may throw an IllegalStateException.
				vertex.removeFromSet();
				return vertex;
			}
		}
		return null; //No vertex with the specified value was found.
	}

	/**
	 * Removes the specified vertex from the graph. The method returns whether
	 * the graph was modified by the method call. If the specified vertex is not
	 * present in the graph, the graph will not be modified and {@code false}
	 * will be returned. Otherwise, the vertex is removed and {@code true} will
	 * be returned.
	 * <p>If removing a vertex causes an arc to have too few connections on any
	 * side, this arc will also automatically be removed. For graphs where
	 * halfarcs are allowed, this is when removing the vertex causes the arc to
	 * have no vertices in its source and destination. For graphs where
	 * halfarcs are not allowed, this is when removing the vertex causes the arc
	 * to have no vertices in either its source or in its destination. If
	 * removing the arc causes the graph to become illegal, an
	 * {@link IllegalStateException} will also be thrown.</p>
	 * @param vertex The vertex that must be removed from the graph.
	 * @return {@code true} if the vertex was present in the graph prior to
	 * removing, or {@code false} otherwise.
	 * @throws IllegalStateException The {@code removeVertex(Vertex)} operation
	 * would cause the graph to become invalid.
	 * @throws NullPointerException The specified vertex is {@code null}.
	 */
	@Override
	public boolean removeVertex(final Vertex<V,A> vertex) {
		if(vertex.graph != this) { //The vertex is not in the graph.
			return false;
		}
		vertex.removeFromGraph(); //This may throw an IllegalStateException.
		vertex.removeFromSet();
		return true;
	}

	/**
	 * Lists the vertices in the source of the specified arc. These are the
	 * vertices the arc is coming from. The vertices are listed in no specified
	 * order.
	 * <p>The set is backed by the graph, so changes to the set are reflected in
	 * the graph and vice-versa.</p>
	 * @param arc The arc to get the source endpoints of.
	 * @return The source endpoints of the specified arc.
	 * @throws NullPointerException The specified arc to get the source
	 * endpoints of is {@code null}.
	 */
	@Override
	public Set<? extends Vertex<V,A>> sourceEndpoints(final net.dulek.collections.graph.arc.Arc<Vertex<V,A>,A> arc) {
		return arc.sourceEndpoints();
	}

	/**
	 * Returns a {@code String} representation of the graph. The string is a
	 * a concatenation of the string representations of the graph's vertices,
	 * separated by newline characters.
	 * @return A string representation of the graph.
	 */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(numVertices() * 14 + (numArcs() << 4)); //Every vertex takes 13 chars plus 4 for every of its outgoing arcs. Assume each arc is mentioned once. One extra for newline.
		final Iterator<Vertex<V,A>> it = vertices.iterator();
		if(it.hasNext()) { //Don't prepend the first vertex with a newline.
			result.append(it.next().toString());
			while(it.hasNext()) { //Print all the rest with newlines before them.
				result.append('\n');
				result.append(it.next().toString());
			}
		}
		return result.toString();
	}

	/**
	 * Provides a set-view of the labels on all the vertices in the graph. The
	 * set is <strong>not</strong> backed by the graph. The set is generated at
	 * the moment this method is called and changes in the set are not reflected
	 * in the graph. More importantly, changes in the graph are not reflected in
	 * the set, so the graph does not need to re-compute whether a label is
	 * still in the graph after removing the label, to keep this set up-to-date.
	 * The set has no particular order (unless an implementation of the
	 * {@code Graph} further specifies it).
	 * @return A set of the labels of all vertices in the graph.
	 */
	public Set<V> vertexLabels() {
		final Set<V> result = new HashSet<>(vertices.size()); //In the worst case, all vertices have a different label. This might be dumb memory-wise, but speedwise it doesn't make much difference.
		for(final Vertex<V,A> vertex : vertices) {
			result.add(vertex.getLabel()); //Set automatically removes duplicates.
		}
		return result;
	}

	/**
	 * Provides a set-view of the vertices in the graph. The set is backed by
	 * the graph, so changes to the graph are reflected in the set and
	 * vice-versa. If the graph or the set is modified while an iteration over
	 * the set is in progress, the result is unspecified. The set has no
	 * particular order (unless a subclass of the {@code Graph} further
	 * specifies it).
	 * @return A set of all vertices in the graph.
	 */
	@Override
	public Set<? extends Vertex<V,A>> vertices() {
		return vertices;
	}

	/**
	 * Provides a set-view of the vertices in the graph with the specified
	 * label. The set is <strong>not</strong> backed by the graph. The set is
	 * generated at the moment this method is called and changes in the set are
	 * not reflected in the graph. More importantly, changes in the graph are
	 * not reflected in the set, so the graph does not need to maintain a
	 * reference to all sets of vertices with every label in the graph, and
	 * change the sets every time a label is changed. The set has no particular
	 * order (unless a subclass of the {@code Graph} further specifies it).
	 * @param label The label of the vertices to provide a set of.
	 * @return A set of all vertices with the specified label.
	 */
	public Set<? extends Vertex<V,A>> vertices(final V label) {
		final Set<Vertex<V,A>> result = new IdentityHashSet<>();
		if(label == null) { //If outside of loop for speed.
			for(final Vertex<V,A> vertex : vertices) { //Add vertices one by one.
				if(vertex.getLabel() == null) { //Matches specified label.
					result.add(vertex);
				}
			}
			return result;
		}
		for(final Vertex<V,A> vertex : vertices) { //Add vertices one by one.
			if(label.equals(vertex.getLabel())) { //Matches specified label.
				result.add(vertex);
			}
		}
		return result;
	}

	/**
	 * Adds the specified arc to the set of arcs of this graph. The arc is
	 * only added to the arc set of the graph. Other elements of the graph are
	 * untouched. If the operation would cause the graph to become invalid, an
	 * {@code IllegalStateException} is thrown.
	 * @param arc The arc to add to the set of arcs of this graph.
	 * @throws IllegalStateException Adding the arc to the arc set would cause
	 * the graph to become invalid.
	 */
	protected void addInternal(final Arc<V,A> arc) {
		arcs.addInternal(arc);
	}

	/**
	 * Adds the specified vertex to the set of vertices of this graph. The
	 * vertex is only added to the vertex set of the graph. Other elements of
	 * the graph are untouched. If the operation would cause the graph to become
	 * invalid, an {@code IllegalStateException} is thrown.
	 * @param vertex The vertex to add to the set of vertices of this graph.
	 * @throws IllegalStateException Adding the vertex to the vertex set would
	 * cause the graph to become invalid.
	 */
	protected void addInternal(final Vertex<V,A> vertex) {
		vertices.addInternal(vertex);
	}

	/**
	 * Clears the set of arcs of this graph. Only the arc set is cleared. Other
	 * elements of the graph are untouched. If the operation would cause the
	 * graph to become invalid, an {@code IllegalStateException} is thrown.
	 * @throws IllegalStateException Clearing the arc set would cause the graph
	 * to become invalid.
	 */
	protected void clearArcsInternal() {
		arcs.clearInternal();
	}

	/**
	 * Clears the set of vertices of this graph. Only the vertex set is cleared.
	 * Other elements of the graph are untouched. If the operation would cause
	 * the graph to become invalid, an {@code IllegalStateException} is thrown.
	 * @throws IllegalStateException Clearing the vertex set would cause the
	 * graph to become invalid.
	 */
	protected void clearVerticesInternal() {
		vertices.clearInternal();
	}

	/**
	 * Removes the specified arc from the set of arcs of this graph. The arc is
	 * only removed from the arc set of the graph. If the operation would cause
	 * the graph to become invalid, an {@code IllegalStateException} is thrown.
	 * @param arc The arc to remove from the set of arcs of this graph.
	 * @throws IllegalStateException Removing the arc from the arc set would
	 * cause the graph to become invalid.
	 */
	protected void removeInternal(final Arc<V,A> arc) {
		arcs.removeInternal(arc);
	}

	/**
	 * Removes the specified vertex from the set of vertices of this graph. The
	 * vertex is only removed from the vertex set of the graph. If the operation
	 * would cause the graph to become invalid, an {@code IllegalStateException}
	 * is thrown.
	 * @param vertex The vertex to remove from the set of vertices of this
	 * graph.
	 * @throws IllegalStateException Removing the vertex from the vertex set
	 * would cause the graph to become invalid.
	 */
	protected void removeInternal(final Vertex<V,A> vertex) {
		vertices.removeInternal(vertex);
	}

	/**
	 * Creates a deep copy of the graph, but instead of returning the graph
	 * itself, it returns the arc corresponding to one of the arcs of the
	 * original graph. Please note that the entire graph will be copied.
	 * @param viewpoint The arc in the original graph whose copy must be
	 * returned.
	 * @return The copy of the specified arc.
	 */
	protected Arc<V,A> clone(final Arc<V,A> viewpoint) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Creates a deep copy of the graph, but instead of returning the graph
	 * itself, it returns the vertex corresponding to one of the vertices of the
	 * original graph. Please note that the entire graph will be copied.
	 * @param viewpoint The vertex in the original graph whose copy must be
	 * returned.
	 * @return The copy of the specified vertex.
	 */
	protected Vertex<V,A> clone(final Vertex<V,A> viewpoint) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * This set contains the arcs in a graph. It is returned by the
	 * {@link #arcs()} method. This implementation ensures that modifying the
	 * arc set will modify the accompanying graph accordingly.
	 * <p>The implementation of the set is an extension of the
	 * {@link IdentityHashSet} implementation for fast access and light-weight
	 * memory use. There are some differences to make the set more suitable for
	 * arcs and to make the set interact with the graph when it is modified.
	 * Every modification to the set must modify the graph in the same way.</p>
	 * <p>This set cannot contain the {@code null}-element.</p>
	 * @see net.dulek.collections.HashSet
	 * @see net.dulek.collections.IdentityHashSet
	 */
	protected class ArcSet extends IdentityHashSet<Arc<V,A>> {
		/**
		 * The version of the serialised format of the set. This identifies a
		 * serialisation as being the serialised format of this class. It needs
		 * to be different from the original serialisation of {@link HashSet}
		 * since it cannot contain {@code null}-elements.
		 */
		//private static final long serialVersionUID = 830717658106238871L;

		/**
		 * Adds the specified arc to this set if it is not already present. If
		 * this set already contains the arc, the call leaves the set unchanged
		 * and returns {@code false}.
		 * <p>The arc will also be added to the corresponding graph.</p>
		 * @param arc The arc to be added to this set.
		 * @return {@code true} if this set did not already contain the
		 * specified arc, or {@code false} otherwise.
		 * @throws IllegalStateException Adding the arc to the set caused the
		 * graph to become invalid.
		 * @throws NullPointerException The specified arc to add was
		 * {@code null}.
		 */
		@Override
		public boolean add(final Arc<V,A> arc) {
			if(arc == null) { //Don't try to add null. Null is not allowed in this set.
				throw new NullPointerException("Trying to add null to the ArcSet.");
			}
			if(arc.graph == Graph.this) {
				return false;
			}
			if(arc.graph != null) {
				throw new IllegalStateException("The specified arc is already in a graph.");
			}
			final Object object = arc;
			final int tMax = table.length - 1;
			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(object) & tMax; //Compute the hash code for its memory address.

			int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
			Object elem;
			//Search for a location to put the element.
			while((elem = table[hash]) != null && elem != tombstone) {
				hash = (hash + offset++) & tMax;
			}
			//table[hash] is now null or a tombstone (since the while loop ended) so this is a free spot.
			table[hash] = object;
			arc.graph = Graph.this;
			modCount++;
			if(++size > treshold) { //Getting too big.
				resize(table.length << 1);
			}
			return true;
		}

		/**
		 * Adds all of the arcs in the specified collection to this set. If the
		 * specified collection is this set, a
		 * {@link ConcurrentModificationException} is thrown.
		 * {@code null}-elements will not be added to this set and will be
		 * skipped if they are present in the specified collection.
		 * <p>This implementation iterates over the specified collection, and
		 * adds each arc returned by the iterator to this collection, in turn.
		 * </p>
		 * <p>The arcs will also be added to the corresponding graph.</p>
		 * @param c The collection containing elements to be added to this set.
		 * @return {@code true} if this set changed as a result of the call.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by checking and updating the graph field of arcs. In some cases, this exception will be caught, but not all.
		public boolean addAll(final Collection<? extends Arc<V,A>> c) {
			if(c == null) {
				throw new NullPointerException("The specified collection to add to the set is null.");
			}
			//Make sure we have enough capacity, even if they're all new elements.
			if(size + c.size() >= treshold) {
				if(table.length == maximumCapacity) { //We're already at maximum capacity. Don't trigger this again.
					treshold = Integer.MAX_VALUE;
				} else {
					final int newCapacity = net.dulek.math.Math.roundUpPower2((int)((size + c.size()) / loadFactor));
					resize(newCapacity);
					//Add all elements, but don't check for tombstones or the treshold (since we just rehashed everything anyways).
					boolean modified = false;
					final int tMax = newCapacity - 1;
					ADDINGALL:
					for(Object element : c) {
						if(element == null) { //Don't try to add null. Skip this element.
							continue;
						}

						final Arc<V,A> arc;
						try {
							arc = (Arc<V,A>)element; //ClassCastException would be caught if it occurs.
						} catch(final ClassCastException e) {
							continue; //Don't try to add this element then. It is not a proper arc.
						}
						if(arc.graph != null) { //Don't try to add arcs already in this or any other graph.
							continue;
						}

						//Compute the desired bucket for the element.
						int hash = System.identityHashCode(element) & tMax;

						int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
						//Search for the element.
						while(table[hash] != null) {
							hash = (hash + offset++) & tMax;
						}
						//table[hash] is now null (since the while loop ended) so this is a free spot.
						table[hash] = element; //Place it at our free spot.
						arc.graph = Graph.this;
						size++;
						modified = true;
					}
					if(modified) {
						modCount++;
						return true;
					}
					return false;
				}
			}

			//Add all elements, but keep checking for tombstones.
			boolean modified = false;
			for(Arc<V,A> element : c) { //Add all elements from the collection.
				modified |= add(element);
			}
			return modified;
		}

		/**
		 * Removes all of the arcs from the graph. The graph will retain its
		 * vertices, but all vertices will be disconnected after this call
		 * returns.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by casting elements of the table to Arc once they've been found.
		public void clear() {
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			final Object[] newTable = new Object[t.length]; //The new table will be completely filled with nulls.
			for(int i = t.length - 1;i >= 0;i--) {
				final Object elem = t[i];
				if(elem != null && elem != tombstone) { //Unlink every individual arc from the graph.
					try {
						((Arc<V,A>)elem).removeFromGraph(); //Unchecked cast is safe since the table contains only Arcs.
					} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(;i < t.length;i++) { //Count back up from where we were.
							final Arc<V,A> elem2 = (Arc<V,A>)elem; //Unchecked cast is safe since the table contains only Arcs.
							for(final Vertex<V,A> vertex : elem2.sourceEndpoints()) { //Re-link all arcs.
								vertex.addToOutgoingInternal(elem2);
							}
							for(final Vertex<V,A> vertex : elem2.destinationEndpoints()) {
								vertex.addToOutgoingInternal(elem2);
							}
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			modCount++;
			table = newTable; //All is well. Swap for the new table.
			for(int i = t.length - 1;i >= 0;i--) { //We still have the original table.
				final Object elem = t[i];
				if(elem != null && elem != tombstone) { //Empty the graph field of every individual arc.
					((Arc<V,A>)elem).graph = null;
				}
			}
			size = 0;
		}

		/**
		 * Returns a deep copy of this set of arcs. To achieve this, the entire
		 * graph is copied and the arcs of the copy are returned. The new set
		 * will be completely separate from the old set, in that no references
		 * to the graph of the old set are maintained and that modifying the new
		 * set will not modify the old set or its graph in any way or vice
		 * versa.
		 * @return A copy of this set of arcs, which is placed in a new graph.
		 * @throws CloneNotSupportedException Cloning is supported, but
		 * this allows extensions of this class to not support cloning.
		 */
		@Override
		@SuppressWarnings("CloneDoesntCallSuperClone") //This method doesn't call super.clone() directly, but clones the graph and lets that build a new ArcSet.
		public ArcSet clone() throws CloneNotSupportedException {
			return Graph.this.clone().arcs;
		}

		/**
		 * Returns {@code true} if this set contains the specified arc.
		 * @param o The arc whose presence in this set is to be tested.
		 * @return {@code true} if this set contains the specified arc, or
		 * {@code false} otherwise.
		 */
		@Override
		@SuppressWarnings("unchecked") //The ClassCastException is caught if it occurs.
		public boolean contains(Object o) {
			Arc<V,A> arc;
			try { //See if this object is a proper arc first.
				arc = (Arc<V,A>)o;
			} catch(final ClassCastException e) {
				return false; //This object is not an arc at all, so it's not in the set.
			}
			return arc.graph == Graph.this; //If it's in this graph, it must be in the set. No actual set containment checks required.
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
			return new Itr(); //Return the extended iterator instead of HashSet's implementation.
		}

		/**
		 * Removes the specified arc from this set if it is present. Returns
		 * {@code true} if this set contained the specified arc (or
		 * equivalently, if this set changed as a result of the call). This set
		 * will not contain the arc once the call returns.
		 * <p>The arc will also be removed from the corresponding graph.</p>
		 * @param element The arc to be removed from this set, if present.
		 * @return {@code true} if the set contained the specified arc, or
		 * {@code false} otherwise.
		 */
		@Override
		@SuppressWarnings("unchecked") //The ClassCastException is caught if it occurs.
		public boolean remove(Object element) {
			final Arc<V,A> arc;
			try {
				arc = (Arc<V,A>)element;
			} catch(final ClassCastException e) {
				return false; //This object is not an arc at all, so it's not in the set.
			}
			if(arc.graph != Graph.this) { //The arc is not in this graph.
				return false;
			}
			arc.removeFromGraph(); //Unlink the arc. This gives an exception if it is not allowed.

			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

			Object elem;
			int offset = 1;
			while((elem = t[hash]) != null) { //Look for the arc.
				if(elem == element) { //This is the arc we seek. Will never be true for null input.
					t[hash] = tombstone; //R.I.P.
					arc.graph = null;
					modCount++;
					size--;
					return true;
				}
				hash = (hash + offset++) & tMax;
			}
			return false; //This shouldn't be reachable if arc.graph is up to date.
		}

		/**
		 * Removes from this set all of its arcs that are contained in the
		 * specified collection. If the specified collection is also a set, this
		 * operation effectively modifies this set so that its value is the
		 * assymetric set difference of the two sets.
		 * <p>This implementation iterates over either the specified collection
		 * or over the set, based on which is smaller: the size of the
		 * collection or the capacity of the hash table. Since the hash table
		 * iterates over the total size of the table rather than just the
		 * elements, its table capacity is compared rather than the cardinality
		 * of the set. When iterating over the collection, each element of the
		 * collection is removed from the set if it is present. When iterating
		 * over the set, each vertex of the set is removed from the set if it is
		 * also contained in the collection.</p>
		 * <p>The arcs will also be removed from the corresponding graph. If the
		 * act of removing these arcs violates one or more of the constraints on
		 * the graph, an {@code IllegalStateException} will be thrown.</p>
		 * <p>Note that these arcs are removed one by one from the graph, at
		 * each arc checking if the graph is still legal. It is concievable that
		 * constraints on a graph exist that would make removing a certain set
		 * of arcs legal, but removing some subsets of that set illegal. In
		 * those cases, this method should be overwritten to first attempt to
		 * remove all arcs, and then checking at once if the graph is still
		 * legal (and if it is not, undo the changes and throw an
		 * {@code IllegalStateException}).</p>
		 * @param c The collection containing arcs to be removed from this set.
		 * @return {@code true} if this set changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ClassCastException The class of an element of the specified
		 * collection is not a subclass of the arcs in this set.
		 * @throws IllegalStateException The {@code removeAll} operation caused
		 * the graph to become invalid.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		@SuppressWarnings("unchecked") //The ClassCastException is caught if it occurs.
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
					final Arc<V,A> arc;
					try {
						arc = (Arc<V,A>)element;
					} catch(final ClassCastException e) {
						continue; //This is not an arc at all, so it's not in the set. Continue with the next element.
					}
					if(arc.graph != Graph.this) {
						continue; //This arc is not in the graph.
					}

					//Compute the desired bucket for the element.
					int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

					Object elem;
					int offset = 1;
					while((elem = t[hash]) != null) { //Look for the object.
						if(elem == element) { //This is the element we seek. Will never be true for null elements.
							removedElements[numRemovedElements++] = element;
							t[hash] = tombstone; //R.I.P.
							arc.graph = null;
							try {
								arc.removeFromGraph(); //Safe cast, since the hash table contained this element and it contains only Arcs.
							} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
								for(int i = numRemovedElements - 1;i >= 0;i--) {
									final Arc<V,A> removedArc = (Arc<V,A>)removedElements[i]; //Safe cast, since they have all been in the hash table, and the table contains only Arcs.
									add(removedArc); //Add it back to the table.
									for(final Vertex<V,A> vertex : removedArc.sourceEndpoints()) { //Re-connect the arc to its vertices.
										vertex.addToOutgoingInternal(removedArc);
									}
									for(final Vertex<V,A> vertex : removedArc.destinationEndpoints()) {
										vertex.addToIncomingInternal(removedArc);
									}
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
					final Arc<V,A> arc = (Arc<V,A>)elem;
					removedElements[numRemovedElements++] = elem;
					t[i] = tombstone; //R.I.P.
					arc.graph = null;
					try {
						arc.removeFromGraph(); //Safe cast, since the hash table contains only Arcs.
					} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(int j = numRemovedElements - 1;j >= 0;j--) {
							final Arc<V,A> removedArc = (Arc<V,A>)removedElements[j]; //Safe cast, since they have all been in the hash table, and the table contains only Arcs.
							add(removedArc); //Add it back to the table.
							for(final Vertex<V,A> vertex : removedArc.sourceEndpoints()) { //Re-connect the arc to its vertices.
								vertex.addToOutgoingInternal(removedArc);
							}
							for(final Vertex<V,A> vertex : removedArc.destinationEndpoints()) {
								vertex.addToIncomingInternal(removedArc);
							}
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
		 * then iterate over the list, re-adding those elements that were in the
		 * original set.</p>
		 * <p>The removed arcs will also be removed from the corresponding
		 * graph. If this causes one or more constraints on the graph to be
		 * violated, an {@code IllegalStateException} should be thrown.</p>
		 * <p>Note that these arcs are removed one by one from the graph, at
		 * each arc checking if the graph is still legal. It is concievable that
		 * constraints on a graph exist that would make removing a certain set
		 * of arcs legal, but removing some subsets of that set illegal. In
		 * those cases, this method should be overwritten to first attempt to
		 * remove all arcs, and then checking at once if the graph is still
		 * legal (and if it is not, undo the changes and throw an
		 * {@code IllegalStateException}).</p>
		 * @param c The collection to retain the arcs from.
		 * @return {@code true} if this set changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ClassCastException The class of an element of the specified
		 * collection is not a subclass of the arcs of this set.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by the casting of the elements to Arc once they have been found.
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
					final Arc<V,A> arc; //First see if it's even an arc.
					try {
						arc = (Arc<V,A>)element;
					} catch(final ClassCastException e) {
						continue; //This element is not even a proper arc.
					}
					if(arc.graph == Graph.this) { //This arc is in the graph, so it should be retained. Hash it and find it in the table.
						//Compute the desired bucket for the element.
						int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

						Object elem;
						int offset = 1;
						while((elem = t[hash]) != null) { //Look for the arc.
							if(elem == tombstone) { //Copy tombstones over as well, or we'd have to rehash everything.
								newTable[hash] = elem;
							} else if(elem == element) { //Found it! Will never be true for null elements.
								newTable[hash] = elem; //Copy it to the new table.
								t[hash] = tombstone; //Don't find it a second time.
								numRetainedElements++;
								break;
							}
							hash = (hash + offset++) & tMax;
						}
					}
					//Element is not in this graph. Continue with the next element.
				}

				int numRemovedElements = 0;
				final Object[] removedElements = new Object[size - numRetainedElements]; //We need to be able to undo changes in the event of an exception, so keep track of the removed elements.
				for(int i = tMax;i >= 0;i--) { //Place tombstones in the new table where arcs have been removed.
					Object elem;
					if((elem = t[i]) != null && newTable[i] == null) {
						newTable[i] = tombstone;
						removedElements[numRemovedElements++] = elem;
						final Arc<V,A> arc = (Arc<V,A>)elem; //Safe cast, since the table contains only arcs.
						try {
							arc.removeFromGraph();
						} catch(final IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
							for(int j = numRemovedElements - 1;j >= 0;j--) {
								final Arc<V,A> removedArc = (Arc<V,A>)removedElements[j]; //Safe cast, since all these objects were in the table and the table has only Arcs.
								add(removedArc); //Re-add the arc (also restores removedArc.graph).
								for(final Vertex<V,A> vertex : removedArc.sourceEndpoints()) { //Re-connect the arc to its vertices.
									vertex.addToOutgoingInternal(removedArc);
								}
								for(final Vertex<V,A> vertex : removedArc.destinationEndpoints()) {
									vertex.addToIncomingInternal(removedArc);
								}
							}
							throw e; //Pass the original exception on.
						}
						arc.graph = null;
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
					final Arc<V,A> arc = (Arc<V,A>)elem; //Safe cast, since the hash table contains only Arcs.
					try {
						arc.removeFromGraph();
					} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(int j = numRemovedElements - 1;j >= 0;j--) {
							final Arc<V,A> removedArc = (Arc<V,A>)removedElements[j]; //Safe cast, since all these objects were in the table and the table has only Arcs.
							add(removedArc); //Re-add the arc (also restores removedArc.graph).
							for(final Vertex<V,A> vertex : removedArc.sourceEndpoints()) { //Re-connect the arc to its vertices.
								vertex.addToOutgoingInternal(removedArc);
							}
							for(final Vertex<V,A> vertex : removedArc.destinationEndpoints()) {
								vertex.addToIncomingInternal(removedArc);
							}
						}
						throw e; //Pass the original exception on.
					}
					arc.graph = null;
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
		 * Adds the specified arc to this set if it is not already present. The
		 * arc is just added to the set itself. The linkage in the graph will
		 * not be manipulated.
		 * <p>Note that this method does not check if the arc is already
		 * present. If the arc is already present, this produces duplicate
		 * elements in the set. Please check whether {@code arc.graph == null}
		 * evaluates to {@code true}, and if not, remove it from its current
		 * graph first or don't add the arc at all.</p>
		 * @param arc The arc to be added to this set.
		 * @return {@code true} if this set did not already contain the
		 * specified arc, or {@code false} otherwise.
		 */
		protected boolean addInternal(final Arc<V,A> arc) {
			if(arc == null) { //Don't try to add null. Null is not allowed in this set.
				throw new NullPointerException("Trying to add null to the ArcSet.");
			}

			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			final int tMax = t.length - 1;
			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(arc) & tMax; //Compute the hash code for its memory address.

			int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
			Object elem;
			//Search for the element.
			while((elem = t[hash]) != null && elem != tombstone) {
				hash = (hash + offset++) & tMax;
			}
			//t[hash] is now null (since the while loop ended) so this is a free spot.
			t[hash] = arc;
			modCount++;
			if(++size > treshold) { //Getting too big.
				resize(t.length << 1);
			}
			return true;
		}

		/**
		 * Clears the internal set of all arcs. This releases the pointers to
		 * the arcs by their set view, but doesn't remove these arcs from the
		 * vertices of the graph. Only if the vertices of the graph are also
		 * cleared will the memory of these arcs truly be freed. The set will be
		 * empty after this call returns.
		 * <p>Other than the other internal methods, this method also clears the
		 * {@link Arc#graph} field of every arc.</p>
		 */
		@SuppressWarnings("unchecked") //Caused by the casting of the elements to Arc once they have been found.
		protected void clearInternal() {
			//Also remove the arc.graph backlinks.
			modCount++;
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			for(int i = t.length - 1;i >= 0;i--) {
				if(t[i] != null) {
					if(t[i] != tombstone) {
						((Arc<V,A>)t[i]).graph = null; //Erase the backlink. Cast is safe, since the table contains only arcs.
					}
					t[i] = null; //Erase the table.
				}
			}
			size = 0;
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
		@SuppressWarnings("unchecked") //The ClassCastException is caught if it occurs.
		protected boolean removeInternal(Object element) {
			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

			Object elem;
			int offset = 1;
			while((elem = t[hash]) != null) { //Look for the arc.
				if(elem == element) { //This is the arc we seek. Will never be true for null input.
					t[hash] = tombstone; //R.I.P.
					modCount++;
					size--;
					return true;
				}
				hash = (hash + offset++) & tMax;
			}
			return false; //Reached an empty spot without any hit. Not found. This shouldn't happen if arc.graph is up to date.
		}

		/**
		 * This iterator iterates over the arcs of a hash set. The iterator is a
		 * minor modification of the original {@link HashSet.Itr}
		 * implementation. The only difference it that removing an element
		 * through this iterator will also unlink it from the graph the set
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
		protected class Itr extends IdentityHashSet<Arc<V,A>>.Itr {
			/**
			 * Removes from the set the last element returned by this iterator.
			 * This method can be called only once per call to {@link #next()}.
			 * <p>The arc is also removed from the graph.</p>
			 * @throws ConcurrentModificationException The set was structurally
			 * modified between the constructing of this iterator and the
			 * calling of this method.
			 * @throws IllegalStateException The {@link #next()} method has not
			 * yet been called, or the {@code remove()} method has already been
			 * called after the last call to the {@code next()} method.
			 */
			@Override
			@SuppressWarnings("unchecked") //Caused by the casting of the element to Arc to unlink it from the graph.
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
					final Arc<V,A> arc = (Arc<V,A>)elem; //Unsafe cast is allowed since it is an element in the set and the set has only arcs.
					arc.removeFromGraph(); //Unlink the element from the graph as well.
					table[last] = tombstone; //R.I.P.
					arc.graph = null;
					size--;
					modCount++;
					expectedModCount = modCount; //This modification is expected since the iterator made it.
					last = -1; //Set this to negative so the next call doesn't try to remove it again.
				}
			}
		}
	}

	/**
	 * This set contains the vertices in a graph. It is returned by the
	 * {@link #vertices()} method. This implementation ensures that modifying
	 * the vertex set will modify the accompanying graph accordingly.
	 * <p>The implementation of the set is an extension of the
	 * {@link HashSet} implementation for fast access and light-weight memory
	 * use. There are some differences to make the set more suitable for
	 * vertices and to make the set interact with the graph when it is modified.
	 * Rather than computing the actual hash code of a vertex, which is an
	 * expensive operation, this hash set uses the memory address of a vertex as
	 * its hash code. This also eliminates the need for any equality checks,
	 * which are expensive too for vertices. Also, every modification to the set
	 * must modify the graph the same way.</p>
	 * <p>This set cannot contain the {@code null}-element.</p>
	 * @see net.dulek.collections.HashSet
	 */
	protected class VertexSet extends IdentityHashSet<Vertex<V,A>> {
		/**
		 * The version of the serialised format of the set. This identifies a
		 * serialisation as being the serialised format of this class. It needs
		 * to be different from the original serialisation of {@link HashSet}
		 * since it cannot contain {@code null}-elements.
		 */
		//private static final long serialVersionUID = 2836884009293130142L;

		/**
		 * Adds the specified vertex to this set if it is not already present.
		 * If this set already contains the vertex, the call leaves the set
		 * unchanged and returns {@code false}.
		 * <p>The vertex will also be added to the corresponding graph.</p>
		 * @param vertex The vertex to be added to this set.
		 * @return {@code true} if this set did not already contain the
		 * specified vertex, or {@code false} otherwise.
		 * @throws IllegalStateException Adding the vertex would cause the graph
		 * to become invalid.
		 * @throws NullPointerException The specified vertex to add was
		 * {@code null}.
		 */
		@Override
		public boolean add(final Vertex<V,A> vertex) {
			if(vertex == null) { //Don't try to add null. Null is not allowed in this set.
				throw new NullPointerException("Trying to add null to the VertexSet.");
			}
			if(vertex.graph == Graph.this) {
				return false;
			}
			if(vertex.graph != null) {
				throw new IllegalStateException("The specified vertex is already in a graph.");
			}

			final Object object = vertex;
			final int tMax = table.length - 1;
			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(object) & tMax; //Compute the hash code for its memory address.

			int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
			Object elem;
			//Search for the element.
			while((elem = table[hash]) != null && elem != tombstone) {
				hash = (hash + offset++) & tMax;
			}
			//table[hash] is now null or a tombstone (since the while loop ended) so this is a free spot.
			table[hash] = object;
			vertex.graph = Graph.this;
			modCount++;
			if(++size > treshold) { //Getting too big.
				resize(table.length << 1);
			}
			return true;
		}

		/**
		 * Adds all of the vertices in the specified collection to this set. If
		 * the specified collection is this set, a
		 * {@link ConcurrentModificationException} is thrown.
		 * {@code null}-elements will not be added to this set and will be
		 * skipped if they are present in the specified collection.
		 * <p>This implementation iterates over the specified collection, and
		 * adds each vertex returned by the iterator to this collection, in
		 * turn.</p>
		 * <p>The vertices will also be added to the corresponding graph.</p>
		 * @param c The collection containing elements to be added to this set.
		 * @return {@code true} if this set changed as a result of the call.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		@SuppressWarnings("unchecked") //The ClassCastException is caught if it occurs.
		public boolean addAll(final Collection<? extends Vertex<V,A>> c) {
			if(c == null) {
				throw new NullPointerException("The specified collection to add to the set is null.");
			}
			//Make sure we have enough capacity, even if they're all new elements.
			if(size + c.size() >= treshold) {
				if(table.length == maximumCapacity) { //We're already at maximum capacity. Don't trigger this again.
					treshold = Integer.MAX_VALUE;
				} else {
					final int newCapacity = net.dulek.math.Math.roundUpPower2((int)((size + c.size()) / loadFactor));
					resize(newCapacity);
					//Add all elements, but don't check for tombstones or the treshold (since we just rehashed everything anyways).
					boolean modified = false;
					final int tMax = newCapacity - 1;
					ADDINGALL:
					for(Object element : c) {
						if(element == null) { //Don't try to add null. Skip this element.
							continue;
						}

						final Vertex<V,A> vertex;
						try {
							vertex = (Vertex<V,A>)element; //ClassCastException is caught if it occurs.
						} catch(ClassCastException e) {
							continue; //Don't try to add this element then. It is not a proper vertex.
						}
						if(vertex.graph != null) {
							continue; //Don't try to add vertices already in this or any other graph.
						}

						//Compute the desired bucket for the element.
						int hash = System.identityHashCode(element) & tMax;

						int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
						Object elem;
						//Search for the element.
						while((elem = table[hash]) != null) {
							if(elem == element) { //The element is already in the table.
								continue ADDINGALL; //Continue with the next element.
							}
							hash = (hash + offset++) & tMax;
						}
						//table[hash] is now null (since the while loop ended) so this is a free spot.
						table[hash] = element; //Place it at our free spot.
						size++;
						modified = true;
					}
					if(modified) {
						modCount++;
						return true;
					}
					return false;
				}
			}

			//Add all elements, but keep checking for tombstones.
			boolean modified = false;
			for(Vertex<V,A> element : c) { //Add all elements from the collection.
				modified |= add(element);
			}
			return modified;
		}

		/**
		 * Removes all of the vertices from the graph. All arcs are removed from
		 * the graph as well. The graph will be empty after this call returns.
		 */
		@Override
		public void clear() {
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			final Object[] newTable = new Object[t.length]; //The new table will be completely filled with nulls.
			for(int i = t.length - 1;i >= 0;i--) {
				final Object elem = t[i];
				if(elem != null && elem != tombstone) { //Unlink every individual vertex from the graph.
					try {
						((Vertex<V,A>)elem).removeFromGraph(); //Unchecked cast is safe since the table contains only Vertices.
					} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(;i < t.length;i++) { //Count back up from where we were.
							final Vertex<V,A> elem2 = (Vertex<V,A>)elem; //Unchecked cast is safe since the table contains only Vertices.
							for(Arc<V,A> arc : elem2.outgoingArcs()) { //Re-link all vertices.
								arc.addToSourceInternal(elem2);
							}
							for(Arc<V,A> arc : elem2.incomingArcs()) {
								arc.addToDestinationInternal(elem2);
							}
						}
						throw e; //Pass the original exception on.
					}
				}
			}
			modCount++;
			table = newTable; //All is well. Swap for the new table.
			for(int i = t.length - 1;i >= 0;i--) { //We still have the original table.
				final Object elem = t[i];
				if(elem != null && elem != tombstone) { //Empty the graph field of every individual vertex.
					((Vertex<V,A>)elem).graph = null;
				}
			}
			size = 0;
			arcs.clear(); //All arcs now have 0 vertices at both endpoints, so they must all be removed.
		}

		/**
		 * Returns a deep copy of this set of vertices. To achieve this, the
		 * entire graph is copied and the vertices of the copy are returned. The
		 * new set will be completely separate from the old set, in that no
		 * references to the graph of the old set are maintained and that
		 * modifying the new set will not modify the old set or its graph in any
		 * way or vice versa.
		 * @return A copy of this set of vertices, which is placed in a new
		 * graph.
		 * @throws CloneNotSupportedException Cloning is supported, but
		 * this allows extensions of this class to not support cloning.
		 */
		@Override
		@SuppressWarnings("CloneDoesntCallSuperClone") //This method doesn't call super.clone() directly, but clones the graph and lets that rebuild a new VertexSet.
		public VertexSet clone() throws CloneNotSupportedException {
			return Graph.this.clone().vertices;
		}

		/**
		 * Returns {@code true} if this set contains the specified vertex.
		 * @param o The vertex whose presence in this set is to be tested.
		 * @return {@code true} if this set contains the specified vertex, or
		 * {@code false} otherwise.
		 */
		@Override
		@SuppressWarnings("unchecked") //The ClassCastException is caught if it occurs.
		public boolean contains(Object o) {
			final Vertex<V,A> vertex;
			try { //See if it's a proper vertex first.
				vertex = (Vertex<V,A>)o; //Unchecked exception is caught if it occurs.
			} catch(ClassCastException e) {
				return false; //This object is not a vertex at all, so it's not in the set.
			}
			return vertex.graph == Graph.this; //If it's in this graph, it must be in the set. No actual set containment checks required.
		}

		/**
		 * Returns an iterator over the vertices in this set. The vertices are
		 * returned in no particular order, as the order depends on the order in
		 * which they are stored in the hash table, and this is unspecified.
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
		 * @return An iterator over the vertices in this set.
		 */
		@Override
		public Iterator<Vertex<V,A>> iterator() {
			return new Itr(); //Return the extended iterator instead of HashSet's implementation.
		}

		/**
		 * Removes the specified vertex from this set if it is present. Returns
		 * {@code true} if this set contained the specified vertex (or
		 * equivalently, if this set changed as a result of the call). This set
		 * will not contain the vertex once the call returns.
		 * <p>The vertex is also removed from the graph. If removing a vertex
		 * causes an arc to have too few vertices on any side, this arc will
		 * also automatically be removed. For graphs where halfarcs are allowed,
		 * this is when removing the vertex causes the arc to have no vertices
		 * in its source and destination. For graphs where halfarcs are not
		 * allowed, this is when removing the vertex causes the arc to have no
		 * vertices in either its source or in its destination. If removing the
		 * arc causes the graph to become illegal, an
		 * {@link IllegalStateException} will also be thrown.</p>
		 * @param element The vertex to be removed from this set, if present.
		 * @return {@code true} if the set contained the specified vertex, or
		 * {@code false} otherwise.
		 * @throws IllegalStateException The {@code remove(Object)} operation
		 * would cause the graph to become invalid.
		 */
		@Override
		@SuppressWarnings("unchecked") //The ClassCastException is caught if it occurs.
		public boolean remove(Object element) {
			final Vertex<V,A> vertex;
			try {
				vertex = (Vertex<V,A>)element;
			} catch(ClassCastException e) {
				return false; //This object is not a vertex at all, so it's not in the set.
			}
			if(vertex.graph != Graph.this) { //The vertex is not in this graph.
				return false;
			}

			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

			Object elem;
			int offset = 1;
			while((elem = t[hash]) != null) { //Look for the vertex.
				if(elem == element) { //This is the vertex we seek. Will never be true for null input.
					vertex.removeFromGraph();
					t[hash] = tombstone; //R.I.P.
					vertex.graph = null;
					modCount++;
					size--;
					return true;
				}
				hash = (hash + offset++) & tMax;
			}
			return false; //This shouldn't be reachable if vertex.graph is up to date.
		}

		/**
		 * Removes from this set all of its vertices that are contained in the
		 * specified collection. If the specified collection is also a set, this
		 * operation effectively modifies this set so that its value is the
		 * assymetric set difference of the two sets.
		 * <p>This implementation iterates over either the specified collection
		 * or over the set, based on which is smaller: the size of the
		 * collection or the capacity of the hash table. Since the hash table
		 * iterates over the total size of the table rather than just the
		 * elements, its table capacity is compared rather than the cardinality
		 * of the set. When iterating over the collection, each element of the
		 * collection is removed from the set if it is present. When iterating
		 * over the set, each vertex of the set is removed from the set if it is
		 * also contained in the collection.</p>
		 * <p>The vertices are also removed from the graph. If removing a vertex
		 * causes an arc to have too few vertices on any side, this arc will
		 * also automatically be removed. For graphs where halfarcs are allowed,
		 * this is when removing the vertex causes the arc to have no vertices
		 * in its source and destination. For graphs where halfarcs are not
		 * allowed, this is when removing the vertex causes the arc to have no
		 * vertices in either its source or in its destination. If removing the
		 * arc causes the graph to become illegal, an
		 * {@link IllegalStateException} will also be thrown.</p>
		 * <p>Note that these vertices are removed one by one from the graph, at
		 * each vertex checking if the graph is still legal. It is concievable
		 * that constraints on a graph exist that would make removing a certain
		 * set of vertices legal, but removing some subsets of that set illegal.
		 * In those cases, this method should be overwritten to first attempt to
		 * remove all vertices, and then checking at once if the graph is still
		 * legal (and if it is not, undo the changes and throw an
		 * {@code IllegalStateException}).</p>
		 * @param c The collection containing vertices to be removed from this
		 * set.
		 * @return {@code true} if this set changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ClassCastException The class of an element of the specified
		 * collection is not a subclass of the vertices in this set.
		 * @throws IllegalStateException The {@code removeAll(Collection)}
		 * operation would cause the graph to become invalid.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by the casting of the elements to Vertex once they have been found.
		public boolean removeAll(final Collection<?> c) {
			if(c == null) {
				throw new NullPointerException("The specified collection to remove all elements from is null.");
			}
			final Object[] t = table; //Local cache for speed without JIT.
			int numRemovedElements = 0;
			final Object[] removedElements = new Object[Math.min(c.size(),size)]; //We need to be able to undo everything in the event of an exception, so keep track of removed elements.

			//Pick whichever method is fastest:
			if(table.length > c.size() || (c instanceof List && table.length > Math.sqrt(c.size()))) { //Iterate over c, removing all elements from this set. Lists have linear contains() methods, so they get special treatment.
				final int tMax = t.length - 1;

				for(final Object element : c) {
					final Vertex<V,A> vertex;
					try {
						vertex = (Vertex<V,A>)element;
					} catch(ClassCastException e) {
						continue; //This is not an arc at all, so it's not in the set. Continue with the next element.
					}
					if(vertex.graph != Graph.this) {
						continue; //This arc is not in the graph.
					}

					//Compute the desired bucket for the element.
					int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

					Object elem;
					int offset = 1;
					while((elem = t[hash]) != null) { //Look for the object.
						if(elem == element) { //This is the element we seek. Will never be true for null elements.
							t[hash] = tombstone; //R.I.P.
							vertex.graph = null;
							removedElements[numRemovedElements++] = element;
							try {
								vertex.removeFromGraph(); //Safe cast, since the hash table contained this element and it contains only Vertices.
							} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
								for(int i = numRemovedElements - 1;i >= 0;i--) {
									final Vertex<V,A> removedVertex = (Vertex<V,A>)removedElements[i]; //Safe cast, since they have all been in the hash table, and the table contains only Vertices.
									add(removedVertex); //Add it back to the table.
									for(final Arc<V,A> arc : removedVertex.outgoingArcs()) { //Re-connect the vertex to its arcs.
										arc.addToSourceInternal(removedVertex);
									}
									for(final Arc<V,A> arc : removedVertex.incomingArcs()) {
										arc.addToDestinationInternal(removedVertex);
									}
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
					t[i] = tombstone; //R.I.P.
					removedElements[numRemovedElements++] = elem;
					final Vertex<V,A> vertex = (Vertex<V,A>)elem; //Safe cast, since the hash table contains only Vertices.
					vertex.graph = null;
					try {
						vertex.removeFromGraph();
					} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(int j = numRemovedElements - 1;j >= 0;j--) {
							final Vertex<V,A> vert = (Vertex<V,A>)removedElements[j]; //Safe cast, since they have all been in the hash table, and the table contains only Vertices.
							add(vert); //Add it back to the table.
							for(final Arc<V,A> arc : vert.outgoingArcs()) { //Re-connect the vertex to its arcs.
								arc.addToSourceInternal(vertex);
							}
							for(final Arc<V,A> arc : vert.incomingArcs()) {
								arc.addToDestinationInternal(vertex);
							}
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
		 * Retains only the vertices in this set that are contained in the
		 * specified collection. In other words, removes from this set all of
		 * its vertices that are not contained in the specified collection. When
		 * the method call returns, the set will contain the intersection of the
		 * original vertices of the set and the vertices of the collection.
		 * <p>This implementation iterates over the vertices of the set and
		 * checks for each vertex whether it is contained in the specified
		 * collection, removing it if it is not contained.</p>
		 * <p>List collections get special treatment. Their
		 * {@link List#contains(Object)} method is generally linear-time and
		 * their iterator constant per element. Therefore, when encountered with
		 * a list of reasonable size, this method will instead clear the set and
		 * then iterate over the list, re-adding those elements that were in the
		 * original set.</p>
		 * <p>The removed vertices are also removed from the graph. If removing
		 * a vertex causes an arc to have too few vertices on any side, this arc
		 * will also automatically be removed. For graphs where halfarcs are
		 * allowed, this is when removing the vertex causes the arc to have no
		 * vertices in either its source or in its destination. If removing the
		 * arc causes the graph to become illegal, an
		 * {@link IllegalStateException} will also be thrown.</p>
		 * <p>Note that these vertices are removed one by one from the graph, at
		 * each vertex checking if the graph is still legal. It is concievable
		 * that constraints on a graph exist that would make removing a certain
		 * set of vertices legal, but removing some subsets of that set illegal.
		 * In those cases, this method should be overwritten to first attempt to
		 * remove all vertices, and then checking at once if the graph is still
		 * legal (and if it is not, undo the changes and throw an
		 * {@code IllegalStateException}).</p>
		 * @param c The collection to retain the vertices from.
		 * @return {@code true} if this set changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ClassCastException The class of an element of the specified
		 * collection is not a subclass of the vertices of this set.
		 * @throws IllegalStateException The {@code retainAll(Collection)}
		 * operation would cause the graph to become invalid.
		 * @throws NullPointerException The specified collection is
		 * {@code null}.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by the casting of the elements to Vertex once they have been found.
		public boolean retainAll(final Collection<?> c) {
			if(c == null) {
				throw new NullPointerException("The specified collection to retain all vertices from is null.");
			}
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.

			if(c instanceof List && t.length > Math.sqrt(c.size())) { //Lists have linear contains() methods and will get special treatment.
				//Iterate over the list and add all elements that are in both the list and in the original table.
				final Object[] newTable = new Object[t.length];
				final int tMax = t.length - 1;
				int numRetainedElements = 0;

				for(Object element : c) { //See if it's in the original hash table.
					final Vertex<V,A> vertex; //First see if it's even a vertex.
					try {
						vertex = (Vertex<V,A>)element;
					} catch(final ClassCastException e) {
						continue; //This element is not even a proper vertex.
					}
					if(vertex.graph == Graph.this) { //This vertex is in the graph, so it should be retained. Hash it and find it in the table.
						//Compute the desired bucket for the element.
						int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

						Object elem;
						int offset = 1;
						while((elem = t[hash]) != null) { //Look for the vertex.
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
				}
				int numRemovedElements = 0;
				final Object[] removedElements = new Object[size - numRetainedElements]; //We need to be able to undo changes in the event of an exception, so keep track of the removed elements.
				for(int i = tMax;i >= 0;i--) { //Place tombstones in the new table where vertices have been removed and remove their arcs too.
					Object elem;
					if((elem = t[i]) != null && newTable[i] == null) {
						newTable[i] = tombstone;
						removedElements[numRemovedElements++] = elem;
						final Vertex<V,A> vertex = (Vertex<V,A>)elem; //Safe cast, since the table contains only Vertices.
						try {
							vertex.removeFromGraph();
						} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
							for(int j = numRemovedElements - 1;j >= 0;j--) {
								final Vertex<V,A> removedVertex = (Vertex<V,A>)removedElements[j]; //Safe cast, since all these objects were in the table and the table has only Vertices.
								add(removedVertex); //Re-add the vertex.
								for(final Arc<V,A> arc : removedVertex.outgoingArcs()) { //Re-connect the vertex to its arcs.
									arc.addToSourceInternal(removedVertex);
								}
								for(final Arc<V,A> arc : removedVertex.incomingArcs()) {
									arc.addToDestinationInternal(removedVertex);
								}
							}
							throw e; //Pass the original exception on.
						}
						vertex.graph = null;
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
					final Vertex<V,A> vertex = (Vertex<V,A>)elem; //Safe cast, since the table contains only Vertices.
					try {
						vertex.removeFromGraph();
					} catch(IllegalStateException e) { //This was impossible, for some reason. We must undo the changes.
						for(int j = numRemovedElements - 1;j >= 0;j--) {
							final Vertex<V,A> removedVertex = (Vertex<V,A>)removedElements[j]; //Safe cast, since all these objects were in the table and the table has only Vertices.
							add(removedVertex); //Re-add the vertex.
							for(final Arc<V,A> arc : removedVertex.outgoingArcs()) { //Re-connect the vertex to its arcs.
								arc.addToSourceInternal(removedVertex);
							}
							for(final Arc<V,A> arc : removedVertex.incomingArcs()) {
								arc.addToDestinationInternal(removedVertex);
							}
						}
						throw e; //Pass the original exception on.
					}
					vertex.graph = null;
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
		 * Adds the specified vertex to this set if it is not already present.
		 * The vertex is just added to the set itself. The linkage in the graph
		 * will not be manipulated.
		 * <p>Note that this method does not check if the vertex is already
		 * present. If the vertex is already present, this produces duplicate
		 * elements in the set. Please check whether
		 * {@code vertex.graph == null} evaluates to {@code true}, and if not,
		 * remove it from its current graph first or don't add the vertex at
		 * all.</p>
		 * @param vertex The vertex to be added to this set.
		 * @return {@code true} if this set did not already contain the
		 * specified vertex, or {@code false} otherwise.
		 */
		protected boolean addInternal(final Vertex<V,A> vertex) {
			if(vertex == null) { //Don't try to add null. Null is not allowed in this set.
				throw new NullPointerException("Trying to add null to the VertexSet.");
			}

			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			final int tMax = t.length - 1;
			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(vertex) & tMax; //Compute the hash code by its memory address.

			int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
			Object elem;
			//Search for the element.
			while((elem = t[hash]) != null && elem != tombstone) {
				hash = (hash + offset++) & tMax;
			}
			//t[hash] is now null or a tombstone (since the while loop ended) so this is a free spot.
			t[hash] = vertex;
			modCount++;
			if(++size > treshold) { //Getting too big.
				resize(t.length << 1);
			}
			return true;
		}

		/**
		 * Removes all of the vertices from this set. The set will be empty
		 * after this call returns. Only the set itself is cleared. The linkage
		 * in the graph will not be manipulated.
		 * <p>Other than the other internal methods, this method also clears the
		 * {@code Vertex#graph} field of every vertex.
		 */
		@SuppressWarnings("unchecked") //Caused by the casting of the elements to Vertex once they have been found.
		protected void clearInternal() {
			//Also remove the vertex.graph backlinks.
			modCount++;
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			for(int i = t.length - 1;i >= 0;i--) {
				if(t[i] != null) {
					if(t[i] != tombstone) {
						((Vertex<V,A>)t[i]).graph = null; //Erase the backlink. Cast is safe, since the table contains only Vertices.
					}
					t[i] = null; //Erase the table.
				}
			}
			size = 0;
		}

		/**
		 * Removes the specified vertex from this set if it is present. Returns
		 * {@code true} if this set contained the specified vertex (or
		 * equivalently, if this set changed as a result of the call). The
		 * vertex is just removed from the set itself. The linkage of the graph
		 * will not be manipulated.
		 * @param element The vertex to be removed from this set, if present.
		 * @return {@code true} if the set contained the specified vertex, or
		 * {@code false} otherwise.
		 */
		protected boolean removeInternal(final Object element) {
			final Object[] t = table; //Local cache for speed without JIT.
			final int tMax = t.length - 1;

			//Compute the desired bucket for the element.
			int hash = System.identityHashCode(element) & tMax; //Null results in hash code 0.

			Object elem;
			int offset = 1;
			while((elem = t[hash]) != null) { //Look for the vertex.
				if(elem == element) { //This is the vertex we seek. Will never be true for null input.
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
		 * This iterator iterates over the vertices of a hash set. The iterator
		 * is a minor modification of the original {@link HashSet.Itr}
		 * implementation. The only difference it that removing an element
		 * through this iterator will also unlink it from the graph the set
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
		protected class Itr extends IdentityHashSet<Vertex<V,A>>.Itr {
			/**
			 * Removes from the set the last element returned by this iterator.
			 * This method can be called only once per call to {@link #next()}.
			 * <p>The vertex is also removed from the graph. If removing a
			 * vertex causes an arc to have too few vertices on any side, this
			 * arc will also automatically be removed. For graphs where
			 * halfarcs are allowed, this is when removing the vertex causes the
			 * arc to have no vertices in its source and destination. For graphs
			 * where halfarcs are not allowed, this is when removing the vertex
			 * causes to have no vertices in either its source or in its
			 * destination. If removing the arc causes the graph to become
			 * illegal, an {@link IllegalStateException} will also be thrown.
			 * </p>
			 * @throws ConcurrentModificationException The set was structurally
			 * modified between the constructing of this iterator and the
			 * calling of this method.
			 * @throws IllegalStateException The {@link #next()} method has not
			 * yet been called, or the {@code remove()} method has already been
			 * called after the last call to the {@code next()} method, or
			 * removing this vertex causes the graph to become invalid.
			 */
			@Override
			@SuppressWarnings("unchecked") //Caused by the casting of the element to Vertex to unlink it from the graph.
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
					Vertex<V,A> vertex = (Vertex<V,A>)elem; //Unsafe cast is allowed since it is an element in the set and the set has only Vertices.
					vertex.removeFromGraph(); //Unlink the element from the graph as well.
					table[last] = tombstone; //R.I.P.
					vertex.graph = null;
					size--;
					modCount++;
					expectedModCount = modCount; //This modification is expected since the iterator made it.
					last = -1; //Set this to negative so the next call doesn't try to remove it again.
				}
			}
		}
	}
}