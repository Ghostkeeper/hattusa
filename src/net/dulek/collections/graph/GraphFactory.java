//package net.dulek.collections.graph;
//
///**
// * A factory to create graphs with the required specifications.
// * <p>For now, this class is only meant for myself (Ruben) as a reminder of
// * which classes should still be implemented.</p>
// * @author Ruben Dulek
// * @version 1.0
// */
//public class GraphFactory<V,E> {
//	private boolean explicitEdges = true; //Also, has data on edges?
//	private boolean explicitNodes = true; //Also, has data on nodes?
//	private boolean hyper = false;
//	private boolean multi = false;
//	private boolean directed = true;
//	private boolean half = false;
//
//	public Graph<V,E> createGraph() {
//		if(explicitEdges) {
//			if(explicitNodes) {
//				if(hyper) {
//					if(multi) {
//						if(directed) {
//							if(half) {
//								return new HalfDirectedMultiHyperNodeGraph<>();
//							} else {
//								return new WholeDirectedMultiHyperNodeGraph<>();
//							}
//						} else {
//							if(half) {
//								return new HalfUndirectedMultiHyperNodeGraph<>();
//							} else {
//								return new WholeUndirectedMultiHyperNodeGraph<>();
//							}
//						}
//					} else {
//						if(directed) {
//							if(half) {
//								return new HalfDirectedSingleHyperNodeGraph<>();
//							} else {
//								return new WholeDirectedSingleHyperNodeGraph<>();
//							}
//						} else {
//							if(half) {
//								return new HalfUndirectedSingleHyperNodeGraph<>();
//							} else {
//								return new WholeUndirectedSingleHyperNodeGraph<>();
//							}
//						}
//					}
//				} else {
//					if(multi) {
//						if(directed) {
//							if(half) {
//								return new HalfDirectedMultiIncidentNodeGraph<>();
//							} else {
//								return new WholeDirectedMultiIncidentNodeGraph<>();
//							}
//						} else {
//							if(half) {
//								return new HalfUndirectedMultiIncidentNodeGraph<>();
//							} else {
//								return new WholeUndirectedMultiIncidentNodeGraph<>();
//							}
//						}
//					} else {
//						if(directed) {
//							if(half) {
//								return new HalfDirectedSingleIncidentNodeGraph<>();
//							} else {
//								return new WholeDirectedSingleIncidentNodeGraph<>();
//							}
//						} else {
//							if(half) {
//								return new HalfUndirectedSingleIncidentNodeGraph<>();
//							} else {
//								return new WholeUndirectedSingleIncidentNodeGraph<>();
//							}
//						}
//					}
//				}
//			} else {
//				if(half) { //Half-edges are impossible without explicit nodes.
//					throw new IllegalStateException("Trying to create a half-graph without explicit nodes.");
//				}
//				if(hyper) {
//					if(multi) {
//						if(directed) {
//							return new DirectedMultiHyperNodelessGraph<>();
//						} else {
//							return new UndirectedMultiHyperNodelessGraph<>();
//						}
//					} else {
//						if(directed) {
//							return new DirectedSingleHyperNodelessGraph<>();
//						} else {
//							return new UndirectedSingleHyperNodelessGraph<>();
//						}
//					}
//				} else {
//					if(multi) {
//						if(directed) {
//							return new DirectedMultiIncidentNodelessGraph<>();
//						} else {
//							return new UndirectedMultiIncidentNodelessGraph<>();
//						}
//					} else {
//						if(directed) {
//							return new DirectedSingleIncidentNodelessGraph<>();
//						} else {
//							return new UndirectedSingleIncidentNodelessGraph<>();
//						}
//					}
//				}
//			}
//		} else {
//			if(hyper) {
//				throw new IllegalStateException("Trying to create a hypergraph without explicit edges.");
//			}
//			if(multi) {
//				throw new IllegalStateException("Trying to create a multigraph without explicit edges.");
//			}
//			if(half) {
//				throw new IllegalStateException("Trying to create a half-graph without explicit edges.");
//			}
//			if(explicitNodes) {
//				if(directed) {
//					return new DirectedEdgelessNodeGraph<>();
//				} else {
//					return new UndirectedEdgelessNodeGraph<>();
//				}
//			} else {
//				if(directed) {
//					return new DirectedEdgelessNodelessGraph<>();
//				} else {
//					return new UndirectedEdgelessNodelessGraph<>();
//				}
//			}
//		}
//	}
//}