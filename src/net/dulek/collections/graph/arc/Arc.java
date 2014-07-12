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

import java.util.Set;

/**
 * This represents an arc in a graph. An arc is a relation between vertices. It
 * connects the vertices with eachother. Arcs are directed for this interface,
 * and thus have to sides to them: A source and a destination, or, respectively,
 * where the arc comes from and where it goes to.
 * <p>Undirected implementations of the {@code Graph} interface may choose to
 * present the endpoints more ambiguously, but the methods required by this
 * interface must still be properly implemented to work on one side of the arc.
 * That is, the methods working on the source of the arc must always work on the
 * other side as the methods working on the destination of the arc. It is
 * suggested to use an implementation that refers to the same arc from both
 * sides.</p>
 * <p>Depending on the implementation of the {@code Graph} interface, an
 * arc may have multiple vertices in its source or destination (a hyperarc), it
 * may have no vertices in its source or destination (a halfarc), and it may be
 * directed or not. For the latter property, if the arc is undirected, it must
 * behave as if there are two arcs: One connecting vertices {@code A} to
 * vertices {@code B}, and one connecting vertices {@code B} to vertices
 * {@code A}. If one vertex is modified, its corresponding opposite arc will be
 * modified as well.</p>
 * @author Ruben Dulek
 * @param <V> The type of the representations for vertices in this graph. This
 * may be an explicit {@code Vertex} class, or any other piece of information
 * that uniquely identifies a vertex.
 * @param <A> The type of data stored in this arc.
 * @see Graph
 * @see net.dulek.collections.graph.Arc
 * @version 1.0
 */
public interface Arc<V,A> {
	/**
	 * Adds the specified vertex to the set of destination vertices to which
	 * this arc goes. The method returns whether the arc was modified by the
	 * method call. When a vertex is already in the destination of this arc, it
	 * will not be added again and the arc is left unmodified.
	 * <p>This is equivalent to calling
	 * {@code destinationEndpoints.add(vertex)}.</p>
	 * @param vertex The vertex to add to the destination of this arc.
	 * @return {@code true} if the arc was modified by adding the specified
	 * vertex to its destination, or {@code false} otherwise.
	 * @throws IllegalStateException Adding a vertex to the destination of this
	 * arc would result in a hyperarc and hyperarcs are not allowed in this
	 * graph, or would otherwise make the graph invalid.
	 * @throws NullPointerException The specified vertex is {@code null}.
	 */
	boolean addToDestination(V vertex);

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
	 * would result in a hyperarc and hyperarcs are not allowed in this graph,
	 * or would otherwise make the graph invalid.
	 * @throws NullPointerException The specified vertex is {@code null}.
	 */
	boolean addToSource(V vertex);

	/**
	 * Returns the set of all vertices in this arc's destination (where this arc
	 * is 'going to'). If the arc is a hyperarc, the set may have more than one
	 * element. If the arc is a halfarc, the set may be empty.
	 * <p>Modifying the set will modify the graph correspondingly. Changes in
	 * the set are reflected in the graph and vice-versa.</p>
	 * @return A set of all vertices in this arc's destination.
	 */
	Set<V> destinationEndpoints();

	/**
	 * Tests whether some other object is an arc equal to this one. An object is
	 * equal to this arc if and only if:
	 * <ul><li>The object is not {@code null}.</li>
	 * <li>The object is an instance of {@code Arc}.</li>
	 * <li>The label of the specified arc equals the label of this arc.
	 * <li>If this arc is part of a graph:
	 *     <ul><li>The specified arc is also part of a graph.</li>
	 *     <li>The graphs of both arcs are strongly isomorphic.</li>
	 *     <li>The labels on equivalent vertices of the graphs, if any, are
	 *     equal.</li>
	 *     <li>The labels on equivalent arcs of the graphs are equal.</li></ul>
	 * </li>
	 * <li>If this arc is not part of a graph:
	 *     <ul><li>The specified arc is also not part of a graph.</li></ul>
	 * </li>
	 * <p>If the arcs are both part of graphs, the method requires the
	 * computation of whether the two graphs are isomorphic. This may be a
	 * computationally expensive problem for the general case, since no
	 * polynomial-time algorithm is yet known. Some implementations of the
	 * {@link #Graph} interface will have restrictions that allow the
	 * equivalence check to fall in the P-class. For the rest, the
	 * {@link #hashCode()} method should be used to approximate true graph
	 * equivalence, or the {@code equals(Object)} method should be used only on
	 * arcs that are part of small graphs and only provide arcs that are part of
	 * small graphs as argument.</p>
	 * @param obj The object with which to compare.
	 * @return {@code true} if this arc is equal to the specified object, or
	 * {@code false} otherwise.
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Returns the label stored in this arc.
	 * @return The label stored in this arc.
	 */
	A getLabel();

	/**
	 * Returns a hash code for the arc. The hash code must be equal to the hash
	 * code of any object {@code x} for which {@code equals(x)} returns
	 * {@code true}. However, objects {@code x} for which {@code equals(x)}
	 * returns {@code false} need not have a different hash code. The
	 * equivalence of hash codes of arcs should give a reasonable guess at the
	 * equivalence of those arcs.
	 * @return A hash code for the arc.
	 */
	@Override
	int hashCode();

	/**
	 * Returns whether this is a directed arc. This arc is directed if there is
	 * no arc in the graph with the same source vertices as this arc's
	 * destination vertices and the same destination vertices as this arc's
	 * source vertices. In other words, there is no arc with the reverse
	 * direction.
	 * @return {@code true} if this arc is directed, or {@code false} otherwise.
	 */
	boolean isDirected();

	/**
	 * Returns whether this is a reflexive arc. An arc is reflexive if its
	 * source vertices are the same as its destination vertices.
	 * @return {@code true} if this arc is reflexive, or {@code false}
	 * otherwise.
	 */
	boolean isReflexive();

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
	 * this arc would result in a halfarc and halfarcs are not allowed in this
	 * graph, or would otherwise invalidate the graph.
	 * @throws NullPointerException The specified vertex to remove from the
	 * destination is {@code null}.
	 */
	boolean removeFromDestination(V vertex);

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
	 * arc would result in a halfarc and halfarcs are not allowed in this graph,
	 * or would otherwise invalidate the graph.
	 * @throws NullPointerException The specified vertex to remove from the
	 * source is {@code null}.
	 */
	boolean removeFromSource(V vertex);

	/**
	 * Changes the label in this arc to the specified value. The previous label
	 * will be returned.
	 * @param label The new label for the arc.
	 * @return The label of the arc before this method was called.
	 */
	A setLabel(A label);

	/**
	 * Returns a set of all vertices in this arc's source (where this arc is
	 * 'coming from'). If the arc is a hyperarc, the set may have more than one
	 * element. If the arc is a halfarc, the set may be empty.
	 * <p>Modifying the set will modify the graph correspondingly. Changes in
	 * the set are reflected in the graph and vice-versa.</p>
	 * @return A set of all vertices in this arc's source.
	 */
	Set<V> sourceEndpoints();
}