package com.compiler.parser.lr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Builds the canonical collection of LR(1) items (the DFA automaton). Items
 * contain a lookahead symbol.
 */
public class LR1Automaton {

    private final Grammar grammar;
    private final List<Set<LR1Item>> states = new ArrayList<>();
    private final Map<Integer, Map<Symbol, Integer>> transitions = new HashMap<>();
    private String augmentedLeftName = null;

    public LR1Automaton(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public List<Set<LR1Item>> getStates() {
        return states;
    }

    public Map<Integer, Map<Symbol, Integer>> getTransitions() {
        return transitions;
    }

     /**
     * CLOSURE for LR(1): standard algorithm using FIRST sets to compute
     * lookaheads for new items.
     */
    private Set<LR1Item> closure(Set<LR1Item> items) {
        // TODO: Implement the CLOSURE algorithm for a set of LR(1) items.
        // 1. Initialize a new set `closure` with the given `items`.
        // 2. Create a worklist (like a Queue or List) and add all items from `items` to it.
        // 3. Pre-calculate the FIRST sets for all symbols in the grammar.
        // 4. While the worklist is not empty:
        //    a. Dequeue an item `[A -> α • B β, a]`.
        //    b. If `B` is a non-terminal:
        //       i. For each production of `B` (e.g., `B -> γ`):
        //          - Calculate the FIRST set of the sequence `βa`. This will be the lookahead for the new item.
        //          - For each terminal `b` in FIRST(βa):
        //             - Create a new item `[B -> • γ, b]`.
        //             - If this new item is not already in the `closure` set:
        //               - Add it to `closure`.
        //               - Enqueue it to the worklist.
        // 5. Return the `closure` set.

        Set<LR1Item> closure = new HashSet<>(items);
        List<LR1Item> worklist = new ArrayList<>(items);
        Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);
        Map<Symbol, Set<Symbol>> firstSets = computeFirstSetsLocal(epsilon);

        while (!worklist.isEmpty()) {
            LR1Item currentItem = worklist.remove(0);
            Symbol B = currentItem.getSymbolAfterDot();
            if (B != null && B.type == SymbolType.NON_TERMINAL) {
                List<Symbol> beta = new ArrayList<>();
                List<Symbol> right = currentItem.production.getRight();
                int bIndex = currentItem.dotPosition + 1;
                for (int i = bIndex; i < right.size(); i++) {
                    beta.add(right.get(i));
                }
                Symbol lookahead = currentItem.lookahead;
                List<Symbol> betaA = new ArrayList<>(beta);
                betaA.add(lookahead);
                Set<Symbol> firstBetaA = computeFirstOfSequence(betaA, firstSets, epsilon);
                for (Production prod : getProductionsFor(B)) {
                    for (Symbol b : firstBetaA) {
                        LR1Item newItem = new LR1Item(prod, 0, b);
                        if (closure.add(newItem)) {
                            worklist.add(newItem);
                        }
                    }
                }
            }
        }

        return closure;
    }

    /**
     * Compute FIRST of a sequence of symbols.
     */
    private Set<Symbol> computeFirstOfSequence(List<Symbol> seq, Map<Symbol, Set<Symbol>> firstSets, Symbol epsilon) {
        // TODO: Implement the logic to compute the FIRST set for a sequence of symbols.
        // 1. Initialize an empty result set.
        // 2. If the sequence is empty, add epsilon to the result and return.
        // 3. Iterate through the symbols `X` in the sequence:
        //    a. Get `FIRST(X)` from the pre-calculated `firstSets`.
        //    b. Add all symbols from `FIRST(X)` to the result, except for epsilon.
        //    c. If `FIRST(X)` does not contain epsilon, stop and break the loop.
        //    d. If it does contain epsilon and this is the last symbol in the sequence, add epsilon to the result set.
        // 4. Return the result set.

        Set<Symbol> result = new HashSet<>();

        if (seq == null || seq.isEmpty()) {
            result.add(epsilon);
            return result;
        }

        for (int i = 0; i < seq.size(); i++) {
            Symbol X = seq.get(i);
            Set<Symbol> firstX = firstSets.get(X);
            if (firstX == null) firstX = new HashSet<>();

            for (Symbol s : firstX) {
                if (!s.equals(epsilon)) result.add(s);
            }

            if (!firstX.contains(epsilon)) {
                break;
            }

            if (i == seq.size() - 1) {
                result.add(epsilon);
            }
        }

        return result;
    }

    /**
     * GOTO for LR(1): moves dot over symbol and takes closure.
     */
    private Set<LR1Item> goTo(Set<LR1Item> state, Symbol symbol) {
        // TODO: Implement the GOTO function.
        // 1. Initialize an empty set `movedItems`.
        // 2. For each item `[A -> α • X β, a]` in the input `state`:
        //    a. If `X` is equal to the input `symbol`:
        //       - Add the new item `[A -> α X • β, a]` to `movedItems`.
        // 3. Return the `closure` of `movedItems`.

        Set<LR1Item> movedItems = new HashSet<>();

        for (LR1Item item : state) {
            Symbol nextSymbol = item.getSymbolAfterDot();
            if (nextSymbol != null && nextSymbol.equals(symbol)) {
                LR1Item moved = new LR1Item(item.production, item.dotPosition + 1, item.lookahead);
                movedItems.add(moved);
            }
        }

        return closure(movedItems);
    }

    /**
     * Build the LR(1) canonical collection: states and transitions.
     */
    public void build() {
        // TODO: Implement the construction of the canonical collection of LR(1) item sets (the DFA).
        // 1. Clear any existing states and transitions.
        // 2. Create the augmented grammar: Add a new start symbol S' and production S' -> S.
        // 3. Create the initial item: `[S' -> • S, $]`.
        // 4. The first state, `I0`, is the `closure` of this initial item set. Add `I0` to the list of states.
        // 5. Create a worklist (queue) and add `I0` to it.
        // 6. While the worklist is not empty:
        //    a. Dequeue a state `I`.
        //    b. For each grammar symbol `X`:
        //       i. Calculate `J = goTo(I, X)`.
        //       ii. If `J` is not empty and not already in the list of states:
        //          - Add `J` to the list of states.
        //          - Enqueue `J` to the worklist.
        //       iii. Create a transition from the index of state `I` to the index of state `J` on symbol `X`.


        states.clear();
        transitions.clear();
        augmentedLeftName = null;
        Symbol realStart = grammar.getStartSymbol();
        String augName = realStart.name + "'";
        augmentedLeftName = augName;
        Symbol augmentedLeft = new Symbol(augName, SymbolType.NON_TERMINAL);
        Production augmentedProduction = new Production(augmentedLeft, List.of(realStart));
        Symbol dollar = new Symbol("$", SymbolType.TERMINAL);
        LR1Item initialItem = new LR1Item(augmentedProduction, 0, dollar);
        Set<LR1Item> I0 = closure(Set.of(initialItem));
        states.add(I0);
        List<Set<LR1Item>> worklist = new ArrayList<>();
        worklist.add(I0);
        Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);
        Map<Symbol, Set<Symbol>> firstSets = computeFirstSetsLocal(epsilon);
        Set<Symbol> allSymbols = new HashSet<>();
        allSymbols.addAll(grammar.getTerminals());
        allSymbols.addAll(grammar.getNonTerminals());
        while (!worklist.isEmpty()) {
            Set<LR1Item> I = worklist.remove(0);
            int indexI = states.indexOf(I);

            for (Symbol X : allSymbols) {
                Set<LR1Item> J = goTo(I, X);
                if (J.isEmpty()) continue;

                int indexJ = states.indexOf(J);
                if (indexJ == -1) {
                    states.add(J);
                    worklist.add(J);
                    indexJ = states.indexOf(J);
                }

                transitions.computeIfAbsent(indexI, k -> new HashMap<>()).put(X, indexJ);
            }
        }
    }

    // Helper: return productions whose left == nt
    private List<Production> getProductionsFor(Symbol nt) {
        List<Production> list = new ArrayList<>();
        for (Production p : grammar.getProductions()) {
            if (p.getLeft().equals(nt)) list.add(p);
        }
        return list;
    }

    // Helper: compute FIRST sets for all symbols in the grammar (terminals map to themselves)
    private Map<Symbol, Set<Symbol>> computeFirstSetsLocal(Symbol epsilon) {
        Map<Symbol, Set<Symbol>> first = new HashMap<>();

        // initialize
        for (Symbol t : grammar.getTerminals()) {
            Set<Symbol> s = new HashSet<>();
            s.add(t);
            first.put(t, s);
        }

        Symbol dollar = new Symbol("$", SymbolType.TERMINAL);
        if (!first.containsKey(dollar)) {
            Set<Symbol> dset = new HashSet<>();
            dset.add(dollar);
            first.put(dollar, dset);
        }
        for (Symbol nt : grammar.getNonTerminals()) {
            first.put(nt, new HashSet<>());
        }
        for (Symbol nt : grammar.getNonTerminals()) {
            first.put(nt, new HashSet<>());
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production p : grammar.getProductions()) {
                Symbol A = p.getLeft();
                List<Symbol> right = p.getRight();
                // handle epsilon production (right can be size 1 with ε)
                if (right.isEmpty()) {
                    if (first.get(A).add(epsilon)) changed = true;
                    continue;
                }
                boolean allNullable = true;
                for (int i = 0; i < right.size(); i++) {
                    Symbol X = right.get(i);
                    Set<Symbol> firstX = first.get(X);
                    if (firstX == null) firstX = new HashSet<>();
                    // add FIRST(X) \ {epsilon} to FIRST(A)
                    for (Symbol s : firstX) {
                        if (!s.equals(epsilon)) {
                            if (first.get(A).add(s)) changed = true;
                        }
                    }
                    if (!firstX.contains(epsilon)) {
                        allNullable = false;
                        break;
                    }
                }
                if (allNullable) {
                    if (first.get(A).add(epsilon)) changed = true;
                }
            }
        }
        return first;
    }
}
