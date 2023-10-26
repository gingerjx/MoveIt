package searchclient;

import java.util.ArrayDeque;
import java.util.PriorityQueue;

public interface Frontier
{
    void add(State state);
    State pop();
    boolean isEmpty();
    int size();
    String getName();
}

class FrontierBFS implements Frontier
{
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);

    @Override
    public void add(State state)
    {
        this.queue.addLast(state);
    }

    @Override
    public State pop()
    {
        return this.queue.pollFirst();
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public String getName()
    {
        return "breadth-first search";
    }
}

class FrontierDFS implements Frontier
{

    private final ArrayDeque<State> stack = new ArrayDeque<>(65536);

    @Override
    public void add(State state)
    {
        this.stack.add(state);
    }

    @Override
    public State pop()
    {
        return this.stack.pollLast();
    }

    @Override
    public boolean isEmpty()
    {
        return this.stack.isEmpty();
    }

    @Override
    public int size()
    {
        return this.stack.size();
    }

    @Override
    public String getName()
    {
        return "depth-first search";
    }
}

class FrontierBestFirst implements Frontier
{
    private Heuristic heuristic;
    private final PriorityQueue<State> queue;

    public FrontierBestFirst(Heuristic h)
    {
        this.heuristic = h;
        queue = new PriorityQueue<>(65536, h);
    }

    @Override
    public void add(State state)
    {
        this.queue.add(state);
    }

    @Override
    public State pop()
    {
        return this.queue.poll();
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public String getName()
    {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}
