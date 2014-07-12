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

package net.dulek.collections.graph.arc;

import java.util.Collection;
import java.util.Set;

/**
 * This interface represents a graph with explicit arcs. The graph consists of a
 * set of vertices interconnected by arcs. This interface further specifies that
 * the graph must have explicit arcs, and that it must be possible to represent
 * hypergraphs and multigraphs with this. Furthermore, it requires that the
 * graph has data (labels) on the arcs.
 * <p>The graph has two generic types, {@code V} and {@code A}, with two wholly
 * different functions. {@code V} represents the type of object that represents
 * the vertices of the graph. The graph is, in its purest form, just a
 * collection of vertices interconnected by arcs. While this interface specifies
 * that arcs are represented by explicit {@code Arc} instances, it doesn't
 * specify what representation to use for vertices. This is left up to the
 * implementation through this generic type argument. The representation for
 * vertices must uniquely identify a vertex for every instance of the object.
 * The other generic type, {@code A}, specifies the type of data that is stored
 * on the arcs of this graph. If there is no data on the arcs in a particular
 * usage of the graph, this type should be {@link Void}.</p>
 * <p>The graph provides methods to modify the graph by adding and removing
 * vertices, and by adding and removing arcs. It provides methods to access and
 * modify the data on arcs. It provides methods to traverse the graph by listing
 * the adjacent vertices of a vertex, listing the incident arcs of a vertex, and
 * listing the endpoints of an arc. It provides {@link Set} views of all
 * vertices and all arcs, and since arcs now have a label, it provides methods
 * to list or to remove arcs with a specified label. Furthermore, a few helper
 * methods are provided as shortcuts to common operations, such as testing if
 * two vertices are adjacent. Lastly, some methods are provided to test
 * properties of the graph. This way, it may determine whether the instance is
 * actually one of {@code Graph}'s subinterfaces or else may be converted to
 * one.</p>
 * <p>Graphs may put restrictions on the allowed structures. This could
 * invalidate some of the operations on the graph that modify it. For instance,
 * if a specific {@code Graph} implementation must implement a tree, then any
 * new arc between two leaves would create a loop. If this happens, the invalid
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
 * <p>Some implementations of the {@code Graph} interface will not have any way
 * to uniquely represent vertices. For instance, an implementation of the graph
 * may store only which arcs are incident to which other arcs, omitting which
 * vertex instances connect the two. This interface assumes that there must be
 * some form of representation for vertices, but if there isn't, not all methods
 * may be implemented properly and alternatives should be provided to allow
 * modification and usage of the graph. This interface fails to provide adequate
 * methods to modify and use the graph in this case, so a different interface
 * should be used.</p>
 * @author Ruben Dulek
 * @param <V> The type of the representations for vertices in this graph. This
 * may be an explicit {@code Vertex} class, or any other piece of information
 * that uniquely identifies a vertex.
 * @param <A> The type of data stored in the arcs of the graph.
 * @see Arc
 * @see net.dulek.collections.graph.Graph
 * @version 1.0
 */
public interface Graph<V,A> extends net.dulek.collections.graph.Graph<V,Arc<V,A>> {
	//Some methods of the interface are overridden here purely to make the Javadoc more accurate to the arc package.

	/**
	 * Creates a new arc that connects the vertices in {@code from} to the
	 * vertices in {@code to}. The specified label will be stored in the arc.
	 * The newly added arc will be returned.
	 * <p>Some implementations may allow arcs to be hyperarcs or halfarcs.
	 * Therefore, the endpoints of an arc may have zero vertices, or more than
	 * one. Implementations that have undirected edges instead of directed arcs
	 * should behave as if they create two arcs, one in each direction, though
	 * these arcs may have the same representation.</p>
	 * @param from The vertices from which this arc comes. Since this may be a
	 * hyperarc or a halfarc, any number of vertices may be on every endpoint.
	 * @param to The vertices to which this arc goes. Since this may be a
	 * hyperarc or a halfarc, any number of vertices may be on every endpoint.
	 * @param label The label to store in the new arc.
	 * @return The newly created arc that contains the specified label.
	 * @throws IllegalStateException The
	 * {@code addArc(Collection,Collection,A)} operation would cause the graph
	 * to become invalid.
	 */
	Arc<V,A> addArc(Collection<V> from,Collection<V> to,A label);

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
	Set<A> arcLabels();

	/**
	 * Provides a set-view of the arcs in the graph with the specified label.
	 * The set is <strong>not</strong> backed by the graph. The set is generated
	 * at the moment this method is called and changes in the set are not
	 * reflected in the graph. More importantly, changes in the graph are not
	 * reflected in the set, so the graph does not need to maintain a reference
	 * to all sets of arcs with every label in the graph, and change the sets
	 * every time a label is changed. The set has no particular order (unless an
	 * implementation of the {@code Graph} further specifies it).
	 * @param label The label of the arcs to provide a set of.
	 * @return A set of all arcs with the specified label.
	 */
	Set<? extends Arc<V,A>> arcs(A label);

	/**
	 * Tests whether some other object is a graph equal to this one. An object
	 * is equal to this graph if and only if:
	 * <ul><li>The object is not {@code null}.</li>
	 * <li>The object is an instance of {@code Graph}.</li>
	 * <li>The graphs are strongly isomorphic.</li>
	 * <li>The labels on equivalent vertices, if any, are equal.</li>
	 * <li>The labels on equivalent arcs are equal.</li></ul>
	 * These properties make the {@code equals(Object)} method reflexive,
	 * symmetric, transitive, consistent and makes {@code this.equals(null)}
	 * return {@code false}, as required by {@link Object#equals(Object)}.
	 * <p>The method requires the computation of whether the two graphs are
	 * isomorph. This may be a computationally expensive problem for the general
	 * case, since no polynomial-time algorithm is yet known. Some
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
	 * Returns the label of the specified arc.
	 * @param arc The arc to get the label of.
	 * @return The label of the specified arc.
	 * @throws NullPointerException The specified arc to get the label of is
	 * {@code null}.
	 */
	A getLabel(Arc<V,A> arc);

	/**
	 * Returns whether this graph has labels on its arcs. Labels are extra
	 * pieces of data that are unrelated to the graph's adjacency structures.
	 * This interface specifies that the graph has labels on its arcs, so this
	 * method will always return {@code true}.
	 * @return Always returns {@code true}, since this graph has labelled arcs.
	 */
	@Override
	boolean hasLabelledArcs();

	/**
	 * Returns whether or not this graph contains an arc with {@code null} as
	 * its label.
	 * @return {@code true} if there exists an arc in this graph with
	 * {@code null} as its label, or {@code false otherwise}.
	 */
	boolean hasNullOnArcs();

	/**
	 * Removes all arcs with the specified label from the graph, and returns a
	 * set of all removed arcs. If no arcs with the specified label are found,
	 * an empty set will be returned.
	 * @param label The label of the arcs that are to be removed.
	 * @return A set of all removed arcs.
	 * @throws IllegalStateException The {@code removeAllArcsByLabel(A)}
	 * operation would cause the graph to become invalid.
	 */
	Set<? extends Arc<V,A>> removeAllArcsByLabel(A label);

	/**
	 * Removes an arc with the specified label from the graph, and returns it.
	 * If no arc with the specified label is found, {@code null} will be
	 * returned.
	 * @param label The label of the arc that is to be removed.
	 * @return The removed arc, or {@code null} if no such arc was found.
	 * @throws IllegalStateException The {@code removeArcByLabel(A)} operation
	 * would cause the graph to become invalid.
	 */
	Arc<V,A> removeArcByLabel(A label);
}
