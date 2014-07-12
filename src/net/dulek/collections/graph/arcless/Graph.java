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

package net.dulek.collections.graph.arcless;

import java.util.Collection;
import java.util.Set;

/**
 * This interface represents a graph without explicit arcs. The graph consists
 * of a set of vertices interconnected by arcs. This interface further specifies
 * that the graph may not have explicit {@code Arc} objects, and that arcs are
 * represented only by their unique identifier. This may simplify
 * implementations of the graph greatly. To further simplify implementations,
 * this interface does not account for hypergraphs, multigraphs and halfgraphs,
 * and no data (label) can be stored in arcs. This prevents implementations from
 * having to resort to using many look-up tables for things that would normally
 * be stored in an explicit {@code Arc} object.
 * <p>The graph has one generic type: {@code V}. This is the type of objects
 * that represent the vertices of the graph. The graph is, in its purest form,
 * just a set of these vertices interconnected by arcs. The interface does not
 * specify what this form of representation must be. Typically, there will be
 * either a {@code Vertex} object that contains all the necessary information
 * for a vertex, or an integer that uniquely identifies the vertex. Other forms
 * of representation are concievable, so this is left to the implementation.</p>
 * <p>The graph provides methods to modify the graph by adding and removing
 * vertices, and by adding and removing arcs. It provides methods to traverse
 * the graph by listing the adjacent vertices of a vertex or the incident arcs
 * of a vertex. It provides {@link Set} views of all vertices and arcs.
 * Furthermore, a few helper methods are provided as shortcuts to common
 * operations, such as testing if two vertices are adjacent. Lastly, some
 * methods are provided to test properties of the graph. This way, it may
 * determine whether the instance is actually one of {@code Graph}'s
 * subinterfaces or else may be converted to one.</p>
 * <p>Graphs may put restrictions on the allowed structures. This could
 * invalidate some of the operations on the graph that modify it. For instance,
 * if a specific {@code Graph} implementation must implement a tree, then any
 * new arc between two leaves would create a loop. If this happens, the invalid
 * operation should throw an {@link IllegalStateException} and undo any changes
 * already made. If an operation would always be invalid, an alternative method
 * should be provided with the same functionality. This may occur for instance
 * if a graph must always be connected, since new vertices will by default have
 * no incoming or outgoing arcs. An exception to this guideline is the removing
 * of arcs once their linked vertices have been removed. Removing a vertex will
 * always remove all its outgoing and incoming arcs rather than throw an
 * {@link IllegalStateException} for the illegal arcs. If removing that arc is
 * illegal, then an {@link IllegalStateException} will still be thrown.</p>
 * <p>Some implementations of the {@code Graph} interface will not have any way
 * to uniquely represent vertices and/or arcs. For instance, an adjacency list
 * implementation may store in its adjacency lists only other vertices to which
 * a vertex is connected, omitting which arc instances are used to connect them.
 * This interface assumes that there must be some form of representation for
 * vertices and arcs, but if there isn't, not all methods may be implemented
 * properly and alternatives should be provided to allow modification and usage
 * of the graph. This interface works well even without having unique indices
 * for arcs, since it allows arcs to be created with
 * {@link #addArc(Collection,Collection)}, removed with {@link #disconnect(V,V)}
 * and adjacency to be checked with {@link #isAdjacent(V,V)}, all without having
 * to specify unique identifiers for arcs. Without having a representation to
 * uniquely identify vertices however, this interface fails to provide adequate
 * methods to modify and use the graph, so a different interface should be used.
 * </p>
 * @author Ruben Dulek
 * @param <V> The type of the representations for vertices in this graph. This
 * may be an explicit {@code Vertex} class, or any other piece of information
 * that uniquely identifies a vertex.
 * @see net.dulek.collections.graph.Graph
 * @version 1.0
 */
public interface Graph<V> extends net.dulek.collections.graph.Graph<V,Integer> {
	//Some methods of the interface are overridden here purely to make the Javadoc more accurate to arcless Graphs.

	/**
	 * Connects the vertices in {@code from} to the vertices in {@code to}. The
	 * index of the newly created arc will be returned, if any.
	 * <p>Bear in mind that while this method allows multiple vertices on every
	 * endpoint (hyperarcs), or none at all (halfarcs), the graph does not.
	 * Therefore, providing collections with more than or less than one item
	 * must result in an {@code IllegalArgumentException}.</p>
	 * @deprecated {@link #addArc(V,V)} allows the creation of arcs without
	 * creating singleton collections and prevents mistakes with those
	 * collections.
	 * @param from The vertices from which this arc comes. Since this arc may
	 * not be a hyperarc or halfarc, the collection provided should contain
	 * exactly one item.
	 * @param to The vertices to which this arc goes. Since this arc may not be
	 * a hyperarc or halfarc, the collection provided should only contain
	 * exactly one item.
	 * @return The index of the newly created arc.
	 * @throws IllegalArgumentException At least one of the specified
	 * collections of vertices has a size not equal to {@code 1}.
	 * @throws IllegalStateException The {@code addArc(Collection,Collection)}
	 * operation would cause the graph to become invalid.
	 * @throws NullPointerException At least one of the specified collections of
	 * vertices is {@code null}.
	 */
	@Deprecated
	@Override
	Integer addArc(Collection<V> from,Collection<V> to);

	/**
	 * Connects the vertex {@code from} to the vertex {@code to}. This method
	 * should be preferred over {@link #addArc(Collection,Collection)}, since it
	 * skips the creation of singleton collections to store the arc's source and
	 * destination and prevents mistakingly providing multiple source or
	 * destination vertices.
	 * @param from The vertex from which this arc comes.
	 * @param to The vertex to which this arc goes.
	 * @return The index of the newly created arc.
	 * @throws IllegalStateException The {@code addArc(V,V)} operation would
	 * cause the graph to become invalid.
	 * @throws NullPointerException At least one of the specified vertices to
	 * connect an arc with is {@code null}.
	 */
	Integer addArc(V from,V to);

	/**
	 * Gives the destination vertex of the specified arc. This is the vertex the
	 * arc is going towards.
	 * @param arc The arc to get the destination vertex of.
	 * @return The destination vertex of the specified arc.
	 * @throws NullPointerException The specified arc to get the destination
	 * endpoint of is {@code null}.
	 */
	V destinationEndpoint(int arc);

	/**
	 * Lists the vertices in the destination of the specified arc. These are the
	 * vertices the arc is going towards.
	 * <p>Since every arc must have exactly one destination vertex, the size of
	 * the resulting set will always be exactly {@code 1}, and must stay that
	 * way. Therefore, modifying the set is not possible, since any adding or
	 * removing of an element to the set would result in a change in the size of
	 * the set, and therefore make the graph illegal, resulting in an
	 * {@code IllegalStateException}.</p>
	 * @deprecated {@link #destinationEndpoint(int)} returns the destination
	 * vertex of an arc without wrapping it in a set.
	 * {@link #setArcDestination(int,V)} allows for changing the destination of
	 * an arc.
	 * @param arc The arc to get the destination endpoints of.
	 * @return The destination endpoints of the specified arc.
	 * @throws NullPointerException The specified arc to get the destination
	 * endpoints of is {@code null}.
	 */
	@Deprecated
	@Override
	Set<V> destinationEndpoints(Integer arc);

	/**
	 * Removes or modifies arcs such that there exists no more arc with
	 * {@code from} in its source and {@code to} in its destination. Every arc
	 * with {@code from} in its source and {@code to} in its destination will be
	 * removed. Since this graph may not be a hypergraph or a multigraph, the
	 * resulting set will always have at most one arc.
	 * @deprecated {@link disconnectSingle(V,V)} returns the index of the
	 * removed arc, if any, without wrappint it in a set.
	 * @param from The source of the arcs that need to be removed.
	 * @param to The destination of the arcs that need to be removed.
	 * @return A set of all arcs that were removed.
	 * @throws IllegalStateException Removing all arcs between the specified
	 * vertices would cause the graph to become illegal.
	 * @throws NullPointerException At least one of the specified vertices is
	 * {@code null}.
	 */
	@Deprecated
	@Override
	Set<Integer> disconnect(V from,V to);

	/**
	 * Removes an arc, if any exists, with {@code from} in its source and with
	 * {@code to} in its destination, and returns the index of the arc that was
	 * removed. If no such arc exists, the graph is left unmodified and
	 * {@code -1} is returned.
	 * @param from The source of the arc that needs to be removed.
	 * @param to The destination of the arc that needs to be removed.
	 * @return The index of the arc that is removed, or {@code -1} if no such
	 * arc exists.
	 * @throws IllegalStateException Removing the arc between the specified
	 * vertices would cause the graph to become illegal.
	 * @throws NullPointerException At least one of the specified vertices is
	 * {@code null}.
	 */
	int disconnectSingle(V from,V to);

	/**
	 * Tests whether some other object is a graph equal to this one. An object
	 * is equal to this graph if and only if:
	 * <ul><li>The object is not {@code null}.</li>
	 * <li>The object is an instance of {@code Graph}.</li>
	 * <li>The graphs are isomorphic.</li>
	 * <li>The labels on equivalent vertices, if any, are equal.</li></ul>
	 * These properties make the {@code equals(Object)} method reflexive,
	 * symmetric, transitive, consistent and makes {@code x.equals(null)} return
	 * {@code false}, as required by {@link Object#equals(Object)}.
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
	 * Returns whether this graph has labels on its arcs. Labels are extra
	 * pieces of data that are unrelated to the graph's adjacency structures.
	 * This interface specifies that the graph has no labels on its arcs, so
	 * this method will always return {@code false}.
	 * @return Always returns {@code false}, since this graph has no labels on
	 * its arcs.
	 */
	@Override
	boolean hasLabelledArcs();

	/**
	 * Returns whether or not this graph has at least one reflexive arc. A
	 * reflexive arc is an arc that has the same source vertex as its
	 * destination vertex.
	 * @return {@code true} if the graph has at least one reflexive arc, or
	 * {@code false} if it has no reflexive arcs.
	 */
	@Override
	boolean hasReflexiveArcs();

	/**
	 * Returns whether or not the graph is a directed graph. The graph is a
	 * directed graph if for any arc going from source vertex {@code A} to
	 * destination vertex {@code B}, there is no arc going from {@code B} to
	 * {@code A}.
	 * @return {@code true} if the graph is a directed graph, or {@code false}
	 * if it is an undirected graph.
	 */
	@Override
	boolean isDirected();

	/**
	 * Returns whether or not the graph has halfarcs. A halfarc is an arc
	 * without source vertex or without destination vertex. This interface
	 * specifies that there are no halfarcs. Thus, this method will always
	 * return {@code false}.
	 * @return Always returns {@code false}, since this graph cannot contain any
	 * halfarcs.
	 */
	@Override
	boolean isHalf();

	/**
	 * Returns whether or not the graph is a hypergraph. A graph is a hypergraph
	 * when it contains at least one arc that has more than one vertex in its
	 * source or destination. This interface specifies that there are no
	 * hyperarcs. Thus, this method will always return {@code false}.
	 * @return Always returns {@code false}, since this graph cannot contain any
	 * hyperarcs.
	 */
	@Override
	boolean isHyper();

	/**
	 * Returns whether or not the graph is a multigraph. A graph is a multigraph
	 * when there exist any two arcs going from the same vertices and going to
	 * the same vertices. This interface specifies that there are no multiarcs.
	 * Thus, this method will always return {@code false}.
	 * @return Always returns {@code false}, since this graph cannot contain
	 * multiple arcs between two vertices.
	 */
	@Override
	boolean isMulti();

	/**
	 * Removes all vertices from the graph that are also contained in the
	 * specified vertex collection. After this call returns, this graph will
	 * have no vertices in common with the specified collection of vertices.
	 * Vertices that are in the specified collection but not in the graph will
	 * be ignored.
	 * <p>All arcs outgoing or incoming to one of the vertices in the specified
	 * collection will also be removed. If removing one of these arcs causes the
	 * graph to become invalid, an {@code IllegalStateException} will also be
	 * thrown.</p>
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
	Set<V> removeAllVertices(Collection<V> vertices);

	/**
	 * Changes the destination endpoint of the specified arc to a different
	 * vertex. The previous destination endpoint is returned.
	 * @param arc The arc of which the destination must be changed.
	 * @param destination The new destination endpoint of the arc.
	 * @return The destination of the arc prior to calling this method.
	 * @throws IllegalArgumentException The specified arc is not in this graph.
	 * @throws IllegalStateException The {@code setArcDestination(int,V)}
	 * operation would cause the graph to become invalid.
	 * @throws NullPointerException The specified vertex endpoint is
	 * {@code null}.
	 */
	V setArcDestination(int arc,V destination);

	/**
	 * Changes the source endpoint of the specified arc to a different vertex.
	 * The previous source endpoint is returned.
	 * @param arc The arc of which the source must be changed.
	 * @param destination The new source endpoint of the arc.
	 * @return The source of the arc prior to calling this method.
	 * @throws IllegalArgumentException The specified arc is not in this graph.
	 * @throws IllegalStateException The {@code setArcSource(int,V)} operation
	 * would cause the graph to become invalid.
	 * @throws NullPointerException The specified vertex endpoint is
	 * {@code null}.
	 */
	V setArcSource(int arc,V destination);

	/**
	 * Gives the source vertex of the specified arc. This is the vertex the arc
	 * is coming from.
	 * @param arc The arc to get the source vertex of.
	 * @return The source vertex of the specified arc.
	 * @throws NullPointerException The specified arc to get the source endpoint
	 * of is {@code null}.
	 */
	V sourceEndpoint(int arc);

	/**
	 * Lists the vertices in the source of the specified arc. These are the
	 * vertices the arc is coming from.
	 * <p>Since every arc must have exactly one source vertex, the size of the
	 * resulting set will always be exactly {@code 1}, and must stay that way.
	 * Therefore, modifying the set is not possible, since any adding or
	 * removing of an element to the set would result in a change in the size of
	 * the set, and therefore make the graph illegal, resulting in an
	 * {@code IllegalStateException}.</p>
	 * @deprecated {@link #sourceEndpoint(int)} returns the source vertex of an
	 * arc without wrapping it in a set.
	 * {@link #setArcSource(int,V)} allows for changing the source of an arc.
	 * @param arc The arc to get the source endpoints of.
	 * @return The source endpoints of the specified arc.
	 * @throws NullPointerException The specified arc to get the source
	 * endpoints of is {@code null}.
	 */
	@Deprecated
	@Override
	Set<V> sourceEndpoints(Integer arc);
}