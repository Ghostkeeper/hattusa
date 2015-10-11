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

package net.dulek.collections.graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * This interface represents a graph. The graph consists of a set of vertices
 * interconnected by arcs. This interface is intended to be as generic as
 * possible, supporting nearly every type of graph possible. There are no
 * restrictions on the graph: It may contain cycles, may be disjunct, may
 * contain reflexive arcs, and so on. Furthermore, it is intended to represent
 * even the most expressive of graphs. Graphs may be multigraphs, hypergraphs,
 * directed or not, graphs with halfarcs, and may contain data on the vertices
 * as well as the arcs.
 * <p>The {@code Graph} is the root of the graph collection hierarchy. All other
 * graph data structures must implement this interface. As such, the methods
 * defined by this interface must be made available by all graphs.
 * Implementations of the graph may want to specify further restrictions on the
 * graph in order to improve the efficiency of the task at hand.</p>
 * <p>The graph has two generic types, {@code V} and {@code A}. These are the
 * types of the objects that represent respectively the vertices and the arcs of
 * the graph. The graph is, in its purest form, just two collections, one of
 * each of these types. The interface does not specify what this form of
 * representation must be. Typically, there will be either a {@code Vertex}
 * object that contains all the necessary information for a vertex, or an
 * integer that uniquely identifies the vertex. Likewise, there will typically
 * either be an {@code Arc} object that contains all the necessary information
 * for an arc, or an integer that uniquely identifies the arc. Other forms of
 * representation are concievable, so this is left to the implementation.</p>
 * <p>The graph provides methods to modify the graph by adding and removing
 * vertices, and by adding and removing arcs. It provides methods to traverse
 * the graph by listing the adjacent vertices of a vertex, listing the incident
 * arcs of a vertex, and listing the endpoints of an arc. It provides
 * {@link Set} views of all vertices and all arcs. Furthermore, a few helper
 * methods are provided as shortcuts to common operations, such as testing if
 * two vertices are adjacent. Lastly, some methods are provided to test
 * properties of the graph. This way, it may determine whether the instance is
 * actually one of {@code Graph}'s subinterfaces or else may be converted to
 * one.</p>
 * <p>Graphs may put restrictions on their allowed structures. This could
 * invalidate some of the operations on the graph that modify it. For instance,
 * if a specific {@code Graph} implementation must implement a tree, then any
 * new arc would create a loop. If this happens, the invalid operation should
 * throw an {@link IllegalStateException} and undo any changes already made. If
 * an operation would always be invalid, an alternative method should be
 * provided with the same functionality. This may occur for instance if a graph
 * must always be connected, since new vertices will by default have no incoming
 * or outgoing arcs. An exception to this guideline is the removing of arcs once
 * their linked vertices have been removed. When a vertex is removed, and this
 * causes some arc to drop to zero vertices on one side, or zero vertices in
 * total for halfarcs, then that arc must also automatically be removed,
 * throwing an {@link IllegalStateException} if removing that arc is illegal.
 * </p>
 * <p>Some implementations of the {@code Graph} interface will not have any way
 * to uniquely represent vertices and/or arcs. For instance, an adjacency list
 * implementation may store in its adjacency lists only other vertices to which
 * a vertex is connected, omitting which arc instances are used to connect them.
 * This interface assumes that there must be some form of representation for
 * vertices and arcs, but if there isn't, not all methods may be implemented
 * properly and alternatives should be provided to allow modification and usage
 * of the graph. This interface works well even without having a form of
 * representation for arcs, since it allows arcs to be created with
 * {@link #addArc(Collection,Collection)}, removed with {@link #disconnect(V,V)}
 * and adjacency to be checked with {@link #isAdjacent(V,V)}, all without having
 * to specify arcs. Without a representation to uniquely identify vertices
 * however, this interface fails to provide adequate methods to modify and use
 * the graph, so a different interface should be used.</p>
 * @author Ruben Dulek
 * @param <V> The type of the representations for vertices in this graph. This
 * may be an explicit {@code Vertex} class, or any other piece of information
 * that uniquely identifies a vertex.
 * @param <A> The type of the representations for arcs in this graph. This may
 * be an explicit {@code Arc} class, or any other piece of information that
 * uniquely identifies an arc.
 * @version 1.0
 */
public interface Graph<V,A> extends Cloneable,Serializable {
	/**
	 * Creates a new arc that connects the vertices in {@code from} to the
	 * vertices in {@code to}. The newly added arc will be returned.
	 * <p>Some implementations may allow arcs to be hyperarcs or halfarcs.
	 * Therefore, the endpoints of an arc may have zero vertices, or more than
	 * one. Implementations that have undirected edges instead of directed arcs
	 * should behave as if they create two arcs, one in each direction, though
	 * these arcs may have the same representation.</p>
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
	A addArc(Collection<V> from,Collection<V> to);

	/**
	 * Creates a new vertex and adds it to the graph. The vertex will not be
	 * connected to any other vertices, but it will be a member of the graph and
	 * thus will be included in the {@link Set} view of the vertices. The newly
	 * added vertex will be returned.
	 * @return The newly created vertex.
	 * @throws IllegalStateException The {@code addVertex()} operation would
	 * cause the graph to become invalid.
	 */
	V addVertex();

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
	Set<? extends V> adjacentVertices(V vertex);

	/**
	 * Provides a set-view of the arcs in the graph. The set is backed by the
	 * graph, so changes to the graph are reflected in the set and vice-versa.
	 * If the graph or the set is modified while an iteration over the set is in
	 * progress, the result is unspecified. The set has no particular order
	 * (unless an implementation of the {@code Graph} further specifies it).
	 * @return A set of all arcs in the graph.
	 */
	Set<? extends A> arcs();

	/**
	 * Removes all arcs and vertices from the graph, reverting it to its initial
	 * state as it was at the time of its creation.
	 * @throws IllegalStateException The {@code clear()} operation would cause
	 * the graph to become invalid.
	 */
	void clear();

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
	Graph<V,A> clone() throws CloneNotSupportedException;

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
	Set<? extends V> destinationEndpoints(A arc);

	/**
	 * Removes or modifies arcs such that there exists no more arc with
	 * {@code from} in its source and {@code to} in its destination. Every arc
	 * with {@code from} in its source and {@code to} in its destination will be
	 * removed. If there were hyperarcs connecting other vertices with each
	 * other too, then new arcs will be created to ensure that those other
	 * vertices will remain connected. The removed arcs will be returned. If
	 * this is a hypergraph or a multigraph, this set may have more than one
	 * element.
	 * @param from The source of the arcs that need to be removed.
	 * @param to The destination of the arcs that need to be removed.
	 * @return A set of all arcs that were removed.
	 * @throws IllegalStateException Removing all arcs between the specified
	 * vertices would cause the graph to become invalid.
	 * @throws NullPointerException At least one of the specified vertices is
	 * {@code null}.
	 */
	Set<? extends A> disconnect(V from,V to);

	/**
	 * Tests whether some other object is a graph equal to this one. An object
	 * is equal to this graph if and only if:
	 * <ul><li>The object is not {@code null}.</li>
	 * <li>The object is an instance of {@code Graph}.</li>
	 * <li>The graphs are strongly isomorphic.</li>
	 * <li>The labels on equivalent vertices, if any, are equal.</li>
	 * <li>The labels on equivalent arcs, if any, are equal.</li></ul>
	 * These properties make the {@code equals(Object)} method reflexive,
	 * symmetric, transitive, consistent and makes {@code this.equals(null)}
	 * return {@code false}, as required by {@link Object#equals(Object)}.
	 * <p>The method requires the computation of whether the two graphs are
	 * isomorphic. This may be a computationally expensive problem for the
	 * general case, since no polynomial-time algorithm is yet known. Some
	 * implementations of this interface will have restrictions that allow the
	 * equivalence check to fall in the P-class. For the rest, the
	 * {@link #hashCode()} method should be used to approximate true graph
	 * equivalence, or the {@code equals(Object)} method should be used only on
	 * small graphs.</p>
	 * @param obj The object with which to compare.
	 * @return {@code true} if this graph is equal to the specified object, or
	 * {@code false} otherwise.
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Computes whether or not the graph has a cycle. A cycle is a path through
	 * the graph, following only arcs in their appropriate direction, that ends
	 * in the same vertex as the one it starts in.
	 * @return {@code true} if the graph has at least one cycle, or
	 * {@code false} if there are no cycles.
	 */
	boolean hasCycle();

	/**
	 * Returns whether this graph has labels on its arcs. Labels are extra
	 * pieces of data that are unrelated to the graph's adjacency structures.
	 * @return {@code true} if the graph has labels on its arcs, or
	 * {@code false} if it doesn't.
	 */
	boolean hasLabelledArcs();

	/**
	 * Returns whether or not this graph has labels on its vertices. Labels are
	 * extra pieces of data that are unrelated to the graph's adjacency
	 * structures.
	 * @return {@code true} if the graph has labels on its vertices, or
	 * {@code false} if it doesn't.
	 */
	boolean hasLabelledVertices();

	/**
	 * Returns a hash code for the graph. The hash code must be equal to the
	 * hash code of any object {@code x} for which {@code equals(x)} returns
	 * {@code true}. However, objects {@code x} for which {@code equals(x)}
	 * returns {@code false} need not have a different hash code. The
	 * equivalence of hash codes of graphs should give a reasonable guess at the
	 * equivalence of those graphs.
	 * @return A hash code for the graph.
	 */
	@Override
	int hashCode();

	/**
	 * Returns whether or not this graph has at least one reflexive arc. A
	 * reflexive arc is an arc in which the source has at least one vertex in
	 * common with the destination.
	 * @return {@code true} if the graph has at least one reflexive arc, or
	 * {@code false} if it has no reflexive arcs.
	 */
	boolean hasReflexiveArcs();

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
	Set<? extends A> incomingArcs(V vertex);

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
	boolean isAdjacent(V vertex,V other);

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
	boolean isConnected(V vertex,V other);

	/**
	 * Returns whether or not the graph is a directed graph. The graph is a
	 * directed graph if for any arc going from source vertices {@code A} to
	 * destination vertices {@code B}, there is no arc going from {@code B} to
	 * {@code A}. In the case of multigraphs, if multiple arcs are identical,
	 * then there must be an equal number of arcs going in the opposite
	 * direction for the arc to remain undirected.
	 * @return {@code true} if the graph is a directed graph, or {@code false}
	 * if it is an undirected graph.
	 */
	boolean isDirected();

	/**
	 * Returns whether or not the graph has halfarcs. A halfarc is an arc
	 * without source vertices or without destination vertices.
	 * @return {@code true} if the graph contains a halfarc, or {@code false} if
	 * it doesn't.
	 */
	boolean isHalf();

	/**
	 * Returns whether or not the graph is a hypergraph. A graph is a hypergraph
	 * when it contains at least one arc that has more than one vertex in its
	 * source or destination.
	 * @return {@code true} if the graph contains a hyperarc, or {@code false}
	 * if it doesn't.
	 */
	boolean isHyper();

	/**
	 * Returns whether or not the graph is a multigraph. A graph is a multigraph
	 * when there exist any two arcs going from the same vertices and going to
	 * the same vertices.
	 * @return {@code true} if the graph has multiple arcs going between the
	 * same vertices.
	 */
	boolean isMulti();

	/**
	 * Returns whether or not the graph is a subgraph of some other graph. The
	 * graph {@code G} is a subgraph of the specified graph {@code other} if and
	 * only if there exists a subset of the vertex set of {@code other} that is
	 * isomorphic to {@code G}.
	 * @param other The graph that must be checked whether this graph is a
	 * subgraph of it.
	 * @return {@code true} if this graph is a subgraph of {@code other}, or
	 * false otherwise.
	 * @throws NullPointerException The specified graph is {@code null}.
	 */
	boolean isSubgraphOf(Graph<V,A> other);

	/**
	 * Gives the total number of arcs in the graph. This is equivalent to
	 * {@code arcs().size()}.
	 * @return The total number of arcs in the graph.
	 */
	int numArcs();

	/**
	 * Gives the total number of vertices in the graph. This is equivalent to
	 * {@code vertices().size()}.
	 * @return The total number of vertices in the graph.
	 */
	int numVertices();

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
	Set<? extends A> outgoingArcs(V vertex);

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
	Set<? extends A> removeAllArcs(Collection<A> arcs);

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
	Set<? extends V> removeAllVertices(Collection<V> vertices);

	/**
	 * Removes the specified arc from the graph. The method returns whether the
	 * graph was modified by the method call. If the specified arc is not
	 * present in the graph, the graph will not be modified and {@code false}
	 * will be returned. Otherwise, the arc is removed and {@code true} will be
	 * returned.
	 * @param arc The arc that must be removed from the graph.
	 * @return {@code true} if the arc was present in the graph prior to
	 * removing, or {@code false} otherwise.
	 * @throws IllegalStateException The {@code removeArc(A)} operation would
	 * cause the graph to become invalid.
	 * @throws NullPointerException The specified arc is {@code null}.
	 */
	boolean removeArc(A arc);

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
	 * @throws IllegalStateException The {@code removeVertex(V)} operation would
	 * cause the graph to become invalid.
	 * @throws NullPointerException The specified vertex is {@code null}.
	 */
	boolean removeVertex(V vertex);

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
	Set<? extends V> sourceEndpoints(A arc);

	/**
	 * Returns the strongly connected components of this graph. The strongly
	 * connected components are the maximal subsets of vertices of the graph
	 * such that in the subgraph induced by them, every pair of vertices
	 * {@code u} and {@code v} is connected by a path from {@code u} to
	 * {@code v} and from {@code v} to {@code u}.
	 * <p>Note that this may be different from the weakly connected components,
	 * which are the maximal subsets of vertices of the graph such that every
	 * pair of vertices {@code u} and {@code v} would be connected by a path
	 * from {@code u} to {@code v} and from {@code v} to {@code u} if all arcs
	 * would be replaced by undirected edges. In an undirected graph, the two
	 * are equivalent.</p>
	 * <p>The result is returned in the form of a set of strongly connected
	 * components, where every strongly connected component is represented by a
	 * set of vertices. If the graph has no vertices, the result is an empty
	 * set. All vertices must be contained in exactly one strongly connected
	 * component.</p>
	 * @return The strongly connected components of this graph.
	 */
	Set<Set<? extends V>> stronglyConnectedComponents();

	/**
	 * Returns a {@code String} representation of the graph. The string should
	 * represent the graph in one of two ways.
	 * <p>For planar graphs, the string may provide an ASCII-drawing of the
	 * graph itself, provided the string representations of the labels on the
	 * vertices and arcs are short enough to put inside the drawing. Even for
	 * planar graphs, the implementation may also choose to return the other
	 * representation of the graph.</p>
	 * <p>Otherwise, the string must provide a description of each of the
	 * graph's vertices. This description must include the label of the vertex
	 * or its index, and to which other vertices it is connected by which arcs.
	 * </p>
	 * @return A string representation of the graph.
	 */
	@Override
	String toString();

	/**
	 * Provides a set-view of the vertices in the graph. The set is backed by
	 * the graph, so changes to the graph are reflected in the set and
	 * vice-versa. If the graph or the set is modified while an iteration over
	 * the set is in progress, the result is unspecified. The set has no
	 * particular order (unless an implementation of the {@code Graph} further
	 * specifies it).
	 * @return A set of all vertices in the graph.
	 */
	Set<? extends V> vertices();

	/**
	 * Returns the weakly connected components of this graph. The weakly
	 * connected components are the maximal subsets of vertices of the graph
	 * such that in the subgraph induced by them, every pair of vertices
	 * {@code u} and {@code v} would be connected by a path from {@code u} to
	 * {@code v} and from {@code v} to {@code u} if all arcs would be replaced
	 * by undirected edges.
	 * <p>Note that this may be different from the strongly connected
	 * components, which are the maximal subsets of vertices of the graph such
	 * that every pair of vertices {@code u} and {@code v} in the subgraph
	 * induced by the subset is already connected by a path from {@code u} to
	 * {@code v} and from {@code v} to {@code u}. In an undirected graph, the
	 * two are equivalent.</p>
	 * <p>The result is returned in the form of a set of weakly connected
	 * components, where every weakly connected component is represented by a
	 * set of vertices. If the graph has no vertices, the result is an empty
	 * set. All vertices must be contained in exactly one weakly connected
	 * component.</p>
	 * @return The weakly connected components of this graph.
	 */
	Set<Set<? extends V>> weaklyConnectedComponents();
}