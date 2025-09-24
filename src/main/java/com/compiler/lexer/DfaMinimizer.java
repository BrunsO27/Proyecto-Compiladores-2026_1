/**
 * DfaMinimizer
 * -------------
 * This class provides an implementation of DFA minimization using the table-filling algorithm.
 * It identifies and merges equivalent states in a deterministic finite automaton (DFA),
 * resulting in a minimized DFA with the smallest number of states that recognizes the same language.
 *
 * Main steps:
 *   1. Initialization: Mark pairs of states as distinguishable if one is final and the other is not.
 *   2. Iterative marking: Mark pairs as distinguishable if their transitions lead to distinguishable states,
 *      or if only one state has a transition for a given symbol.
 *   3. Partitioning: Group equivalent states and build the minimized DFA.
 *
 * Helper methods are provided for partitioning, union-find operations, and pair representation.
 */
package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

/**
 * Implements DFA minimization using the table-filling algorithm.
 */
/**
 * Utility class for minimizing DFAs using the table-filling algorithm.
 */
public class DfaMinimizer {

    /**
     * Default constructor for DfaMinimizer.
     */
    public DfaMinimizer() {
        // TODO: Implement constructor if needed
    }

    /**
     * Minimizes a given DFA using the table-filling algorithm.
     *
     * @param originalDfa The original DFA to be minimized.
     * @param alphabet The set of input symbols.
     * @return A minimized DFA equivalent to the original.
     */
    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
        /*
     Pseudocode:
     1. Collect and sort all DFA states
     2. Initialize table of state pairs; mark pairs as distinguishable if one is final and the other is not
     3. Iteratively mark pairs as distinguishable if their transitions lead to distinguishable states or only one has a transition
     4. Partition states into equivalence classes (using union-find)
     5. Create new minimized states for each partition
     6. Reconstruct transitions for minimized states
     7. Set start state and return minimized DFA
         */
        List<DfaState> allStates = new ArrayList<>(originalDfa.allStates);
        allStates.sort((a, b) -> Integer.compare(a.id, b.id));

        Map<Pair, Boolean> table = new HashMap<>();
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                Pair pair = new Pair(s1, s2);
                //table.put(pair, s1.isFinal() != s2.isFinal());
                table.put(pair, s1.isFinal() != s2.isFinal());
            }
        }

        for (DfaState s : allStates) {
            table.put(new Pair(s, s), false); 
        }

        boolean changed;
        do {
            changed = false;
            for (Pair pair : new ArrayList<>(table.keySet())) {
                if (table.get(pair)) continue;

                for (char symbol : alphabet) {
                    DfaState t1 = pair.s1.getTransition(symbol);
                    DfaState t2 = pair.s2.getTransition(symbol);

                    if (t1 == null && t2 == null) continue;
                    if (t1 == null || t2 == null) {
                        table.put(pair, true);
                        changed = true;
                        break;
                    }

                    Pair targetPair = new Pair(t1, t2);
                    if (table.getOrDefault(targetPair, false)) {
                        table.put(pair, true);
                        changed = true;
                        break;
                    }
                }
            }
        } while (changed);

        List<Set<DfaState>> partitions = createPartitions(allStates, table);

        Map<DfaState, DfaState> representativeMap = new HashMap<>();
        List<DfaState> minimizedStates = new ArrayList<>();
        for (Set<DfaState> group : partitions) {
            DfaState rep = group.iterator().next();
            DfaState newState = new DfaState(rep.getName());
            boolean isFinalGroup = group.stream().anyMatch(DfaState::isFinal);
            newState.setFinal(isFinalGroup);
            minimizedStates.add(newState);

            for (DfaState state : group) {
                representativeMap.put(state, newState);
            }
        }

        for (Set<DfaState> group : partitions) {
            DfaState rep = group.iterator().next();
            DfaState newRep = representativeMap.get(rep);

            for (char symbol : alphabet) {
                DfaState target = rep.getTransition(symbol);
                if (target != null) {
                    newRep.addTransition(symbol, representativeMap.get(target));
                }
            }
        }

        DfaState minimizedStart = representativeMap.get(originalDfa.startState);
        return new DFA(minimizedStart, minimizedStates);
    }

    /**
     * Groups equivalent states into partitions using union-find.
     *
     * @param allStates List of all DFA states.
     * @param table Table indicating which pairs are distinguishable.
     * @return List of partitions, each containing equivalent states.
     */
    private static List<Set<DfaState>> createPartitions(List<DfaState> allStates, Map<Pair, Boolean> table) {
        /*
     Pseudocode:
     1. Initialize each state as its own parent
     2. For each pair not marked as distinguishable, union the states
     3. Group states by their root parent
     4. Return list of partitions
         */
        Map<DfaState, DfaState> parent = new HashMap<>();
        for (DfaState s : allStates) {
            parent.put(s, s);
        }

        for (Pair pair : table.keySet()) {
            if (!table.get(pair)) {
                union(parent, pair.s1, pair.s2);
            }
        }

        Map<DfaState, Set<DfaState>> groups = new HashMap<>();
        for (DfaState s : allStates) {
            DfaState root = find(parent, s);
            groups.computeIfAbsent(root, k -> new HashSet<>()).add(s);
        }

        return new ArrayList<>(groups.values());
    }

    /**
     * Finds the root parent of a state in the union-find structure. Implements
     * path compression for efficiency.
     *
     * @param parent Parent map.
     * @param state State to find.
     * @return Root parent of the state.
     */
    private static DfaState find(Map<DfaState, DfaState> parent, DfaState state) {
        /*
     Pseudocode:
     If parent[state] == state, return state
     Else, recursively find parent and apply path compression
     Return parent[state]
         */
        if (parent.get(state) == state) {
            return state;
        }
        DfaState root = find(parent, parent.get(state));
        parent.put(state, root); // path compression
        return root;
    }

    /**
     * Unites two states in the union-find structure.
     *
     * @param parent Parent map.
     * @param s1 First state.
     * @param s2 Second state.
     */
    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
        /*
     Pseudocode:
     Find roots of s1 and s2
     If roots are different, set parent of one to the other
         */
        DfaState root1 = find(parent, s1);
        DfaState root2 = find(parent, s2);
        if (root1 != root2) {
            parent.put(root2, root1);
        }
    }

    /**
     * Helper class to represent a pair of DFA states in canonical order. Used
     * for table indexing and comparison.
     */
    private static class Pair {

        final DfaState s1;
        final DfaState s2;

        /**
         * Constructs a pair in canonical order (lowest id first).
         *
         * @param s1 First state.
         * @param s2 Second state.
         */
        public Pair(DfaState s1, DfaState s2) {
            /*
             Pseudocode:
             Assign s1 and s2 so that s1.id <= s2.id
             */
            if (s1.id <= s2.id) {
                this.s1 = s1;
                this.s2 = s2;
            } else {
                this.s1 = s2;
                this.s2 = s1;
            }
        }

        @Override
        public boolean equals(Object o) {
            /*
             Pseudocode:
             Return true if both s1 and s2 ids match
             */
            if (!(o instanceof Pair)) {
                return false;
            }

            Pair other = (Pair) o;

            return s1.id == other.s1.id && s2.id == other.s2.id;
        }

        @Override
        public int hashCode() {
            /*
             Pseudocode:
             Return hash of s1.id and s2.id
             */
            return Objects.hash(s1.id, s2.id);
        }
    }
}
