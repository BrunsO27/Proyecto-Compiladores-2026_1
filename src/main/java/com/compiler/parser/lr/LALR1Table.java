package com.compiler.parser.lr;

/**
 * Builds the LALR(1) parsing table (ACTION/GOTO).
 * Main task for Practice 9.
 */
public class LALR1Table {
    private final LR1Automaton automaton;

    // merged LALR states and transitions
    private java.util.List<java.util.Set<LR1Item>> lalrStates = new java.util.ArrayList<>();
    private java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> lalrTransitions = new java.util.HashMap<>();
    
    // ACTION table: state -> terminal -> Action
    public static class Action {
        public enum Type { SHIFT, REDUCE, ACCEPT }
        public final Type type;
        public final Integer state; // for SHIFT
        public final com.compiler.parser.grammar.Production reduceProd; // for REDUCE

        private Action(Type type, Integer state, com.compiler.parser.grammar.Production prod) {
            this.type = type; this.state = state; this.reduceProd = prod;
        }

        public static Action shift(int s) { return new Action(Type.SHIFT, s, null); }
        public static Action reduce(com.compiler.parser.grammar.Production p) { return new Action(Type.REDUCE, null, p); }
        public static Action accept() { return new Action(Type.ACCEPT, null, null); }
    }

    private final java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Action>> action = new java.util.HashMap<>();
    private final java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> gotoTable = new java.util.HashMap<>();
    private final java.util.List<String> conflicts = new java.util.ArrayList<>();
    private int initialState = 0;

    public LALR1Table(LR1Automaton automaton) {
        this.automaton = automaton;
    }

    /**
     * Builds the LALR(1) parsing table.
     */
    public void build() {
        // TODO: Implement the LALR(1) table construction logic.
        // This is a multi-step process.
        
        // Step 1: Ensure the underlying LR(1) automaton is built.
        // automaton.build();

        // Step 2: Merge LR(1) states to create LALR(1) states.
        //  a. Group LR(1) states that have the same "kernel" (the set of LR(0) items).
        //     - A kernel item is an LR(1) item without its lookahead.
        //     - Create a map from a kernel (Set<KernelEntry>) to a list of state IDs that share that kernel.
        //  b. For each group of states with the same kernel:
        //     - Create a single new LALR(1) state.
        //     - This new state is formed by merging the LR(1) items from all states in the group.
        //     - Merging means for each kernel item, the new lookahead set is the union of all lookaheads for that item across the group.
        //     - Store these new LALR states in `lalrStates`.
        //  c. Create a mapping from old LR(1) state IDs to new LALR(1) state IDs.

        // Step 3: Build the transitions for the new LALR(1) automaton.
        //  - For each transition in the original LR(1) automaton `s -X-> t`:
        //  - Add a new transition for the LALR automaton: `merged(s) -X-> merged(t)`.
        //  - Use the mapping from step 2c to find the merged state IDs.
        //  - Store these new transitions in `lalrTransitions`.

        // Step 4: Fill the ACTION and GOTO tables based on the LALR automaton.
        //  - Call a helper method, e.g., `fillActionGoto()`.
        automaton.build();

        lalrStates.clear();
        lalrTransitions.clear();
        action.clear();
        gotoTable.clear();
        conflicts.clear();
        initialState = 0;

        java.util.Map<java.util.Set<KernelEntry>, java.util.List<Integer>> kernelMap = new java.util.HashMap<>();
        java.util.List<java.util.Set<LR1Item>> lrStates = automaton.getStates();

        for (int i = 0; i < lrStates.size(); i++) {
            java.util.Set<LR1Item> state = lrStates.get(i);
            java.util.Set<KernelEntry> kernel = new java.util.HashSet<>();
            for (LR1Item it : state) {
                if (it.dotPosition > 0) {
                    kernel.add(new KernelEntry(it.production, it.dotPosition));
                }
            }

            kernelMap.computeIfAbsent(kernel, k -> new java.util.ArrayList<>()).add(i);
        }

        java.util.Map<Integer, Integer> oldToNew = new java.util.HashMap<>();
        for (java.util.List<Integer> group : kernelMap.values()) {

            java.util.Map<KernelEntry, java.util.Set<com.compiler.parser.grammar.Symbol>> lookaheadMap = new java.util.HashMap<>();
            for (int sid : group) {
                for (LR1Item it : lrStates.get(sid)) {
                    KernelEntry ke = new KernelEntry(it.production, it.dotPosition);
                    lookaheadMap.computeIfAbsent(ke, k -> new java.util.HashSet<>()).add(it.lookahead);
                }
            }


            java.util.Set<LR1Item> merged = new java.util.HashSet<>();
            for (java.util.Map.Entry<KernelEntry, java.util.Set<com.compiler.parser.grammar.Symbol>> e : lookaheadMap.entrySet()) {
                KernelEntry ke = e.getKey();
                for (com.compiler.parser.grammar.Symbol la : e.getValue()) {
                    merged.add(new LR1Item(ke.production, ke.dotPosition, la));
                }
            }

            int newId = lalrStates.size();
            lalrStates.add(merged);
            for (int sid : group) oldToNew.put(sid, newId);
        }

        for (int i = 0; i < lrStates.size(); i++) {
            if (oldToNew.containsKey(i)) continue;

            java.util.Map<KernelEntry, java.util.Set<com.compiler.parser.grammar.Symbol>> lookaheadMap = new java.util.HashMap<>();
            for (LR1Item it : lrStates.get(i)) {
                KernelEntry ke = new KernelEntry(it.production, it.dotPosition);
                lookaheadMap.computeIfAbsent(ke, k -> new java.util.HashSet<>()).add(it.lookahead);
            }
            java.util.Set<LR1Item> merged = new java.util.HashSet<>();
            for (java.util.Map.Entry<KernelEntry, java.util.Set<com.compiler.parser.grammar.Symbol>> e : lookaheadMap.entrySet()) {
                KernelEntry ke = e.getKey();
                for (com.compiler.parser.grammar.Symbol la : e.getValue()) {
                    merged.add(new LR1Item(ke.production, ke.dotPosition, la));
                }
            }
            int newId = lalrStates.size();
            lalrStates.add(merged);
            oldToNew.put(i, newId);
        }
        
        java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> lrTrans = automaton.getTransitions();
        
        for (java.util.Map.Entry<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> entry : lrTrans.entrySet()) {
            Integer from = entry.getKey();
            java.util.Map<com.compiler.parser.grammar.Symbol, Integer> map = entry.getValue();
            for (java.util.Map.Entry<com.compiler.parser.grammar.Symbol, Integer> tr : map.entrySet()) {
                com.compiler.parser.grammar.Symbol sym = tr.getKey();
                Integer to = tr.getValue();
                Integer newFrom = oldToNew.get(from);
                Integer newTo = oldToNew.get(to);
                if (newFrom == null || newTo == null) continue;
                lalrTransitions.computeIfAbsent(newFrom, k -> new java.util.HashMap<>()).put(sym, newTo);
            }
        }

        if (oldToNew.containsKey(0)) initialState = oldToNew.get(0);
        fillActionGoto();
    }


    private void fillActionGoto() {
        // TODO: Populate the ACTION and GOTO tables based on the LALR states and transitions.
        // 1. Clear the action, gotoTable, and conflicts lists.
        // 2. Iterate through each LALR state `s` from 0 to lalrStates.size() - 1.
        // 3. For each state `s`, iterate through its LR1Item `it`.
        //    a. Get the symbol after the dot, `X = it.getSymbolAfterDot()`.
        //    b. If `X` is a terminal (SHIFT action):
        //       - Find the destination state `t` from `lalrTransitions.get(s).get(X)`.
        //       - Check for conflicts: if action table already has an entry for `[s, X]`, it's a conflict.
        //       - Otherwise, set `action[s][X] = SHIFT(t)`.
        //    c. If the dot is at the end of the production (`X` is null) (REDUCE or ACCEPT action):
        //       - This is an item like `[A -> α •, a]`.
        //       - If it's the augmented start production (`S' -> S •`) and lookahead is `$`, this is an ACCEPT action.
        //         - Set `action[s][$] = ACCEPT`.
        //       - Otherwise, it's a REDUCE action.
        //         - For the lookahead symbol `a` in the item:
        //         - Check for conflicts: if `action[s][a]` is already filled, report a Shift/Reduce or Reduce/Reduce conflict.
        //         - Otherwise, set `action[s][a] = REDUCE(A -> α)`.
        // 4. Populate the GOTO table.
        //    - For each state `s`, look at its transitions in `lalrTransitions`.
        //    - For each transition on a NON-TERMINAL symbol `B` to state `t`:
        //    - Set `gotoTable[s][B] = t`.
        action.clear();
        gotoTable.clear();
        conflicts.clear();

        java.util.function.BiConsumer<Integer, com.compiler.parser.grammar.Symbol> ensureActionRow =
            (s, sym) -> action.computeIfAbsent(s, k -> new java.util.HashMap<>());

        for (int s = 0; s < lalrStates.size(); s++) {
            java.util.Set<LR1Item> items = lalrStates.get(s);
            java.util.Map<com.compiler.parser.grammar.Symbol, Integer> trans = lalrTransitions.getOrDefault(s, java.util.Collections.emptyMap());

            for (java.util.Map.Entry<com.compiler.parser.grammar.Symbol, Integer> tr : trans.entrySet()) {
                com.compiler.parser.grammar.Symbol sym = tr.getKey();
                int t = tr.getValue();
                if (sym.type == com.compiler.parser.grammar.SymbolType.TERMINAL) {
                    java.util.Map<com.compiler.parser.grammar.Symbol, Action> row = action.computeIfAbsent(s, k -> new java.util.HashMap<>());
                    Action newAct = Action.shift(t);
                    Action existing = row.get(sym);
                    if (existing != null) {
                        conflicts.add("Conflict at state " + s + " on terminal " + sym.name + ": existing=" + existing.type + ", new=SHIFT");
                    } else {
                        row.put(sym, newAct);
                    }
                }
            }

            
            for (LR1Item it : items) {
                com.compiler.parser.grammar.Symbol X = it.getSymbolAfterDot();
                if (X == null) {
                    
                    com.compiler.parser.grammar.Production prod = it.production;
                    com.compiler.parser.grammar.Symbol look = it.lookahead;

                    boolean isAccept = prod.left.name.endsWith("'") && "$".equals(look.name);
                    java.util.Map<com.compiler.parser.grammar.Symbol, Action> row = action.computeIfAbsent(s, k -> new java.util.HashMap<>());
                    if (isAccept) {
                        Action existing = row.get(look);
                        if (existing != null && existing.type != Action.Type.ACCEPT) {
                            conflicts.add("Conflict at state " + s + " on " + look.name + ": existing=" + existing.type + ", new=ACCEPT");
                        } else {
                            row.put(look, Action.accept());
                        }
                    } else {
                        Action newAct = Action.reduce(prod);
                        Action existing = row.get(look);
                        if (existing != null) {
                            String kind = (existing.type == Action.Type.SHIFT) ? "Shift/Reduce" : "Reduce/Reduce";
                            conflicts.add(kind + " at state " + s + " on " + look.name + " (existing=" + existing.type + ", new=REDUCE)");
                        } else {
                            row.put(look, newAct);
                        }
                    }
                }
            }
            
            java.util.Map<com.compiler.parser.grammar.Symbol, Integer> transMap = lalrTransitions.getOrDefault(s, java.util.Collections.emptyMap());
            for (java.util.Map.Entry<com.compiler.parser.grammar.Symbol, Integer> tr : transMap.entrySet()) {
                com.compiler.parser.grammar.Symbol sym = tr.getKey();
                int t = tr.getValue();
                if (sym.type == com.compiler.parser.grammar.SymbolType.NON_TERMINAL) {
                    gotoTable.computeIfAbsent(s, k -> new java.util.HashMap<>()).put(sym, t);
                }
            }
        }
    }
    
    // ... (Getters and KernelEntry class can remain as is)
    public java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Action>> getActionTable() { return action; }
    public java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> getGotoTable() { return gotoTable; }
    public java.util.List<String> getConflicts() { return conflicts; }
    private static class KernelEntry {
        public final com.compiler.parser.grammar.Production production;
        public final int dotPosition;
        KernelEntry(com.compiler.parser.grammar.Production production, int dotPosition) {
            this.production = production;
            this.dotPosition = dotPosition;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof KernelEntry)) return false;
            KernelEntry o = (KernelEntry) obj;
            return dotPosition == o.dotPosition && production.equals(o.production);
        }
        @Override
        public int hashCode() {
            int r = production.hashCode();
            r = 31 * r + dotPosition;
            return r;
        }
    }
    public java.util.List<java.util.Set<LR1Item>> getLALRStates() { return lalrStates; }
    public java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> getLALRTransitions() { return lalrTransitions; }
    public int getInitialState() { return initialState; }
}
