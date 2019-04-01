/*
 * Copyright (C) 2018 maximen39
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.luckyverse.duels.arena.states;

import net.luckyverse.api.arena.Arena;
import net.luckyverse.api.arena.ArenaStage;

/**
 * @author maximen39
 */
public abstract class AbstractState<A extends Arena<P, A>, P> implements ArenaStage<A, P> {

    private ArenaStage<A, P> previousStage;
    private ArenaStage<A, P> nextStage;
    protected A arena;

    public void setup(ArenaStage<A, P> previousStage, ArenaStage<A, P> nextStage) {
        this.previousStage = previousStage;
        this.nextStage = nextStage;
    }

    @Override
    public ArenaStage<A, P> previousStage() {
        endStage();
        return previousStage;
    }

    public ArenaStage<A, P> nextStage() {
        endStage();
        return nextStage;
    }

    @Override
    public void startStage(A arena) {
        this.arena = arena;
    }
}