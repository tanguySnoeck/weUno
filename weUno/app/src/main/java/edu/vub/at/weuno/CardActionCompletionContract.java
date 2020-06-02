package edu.vub.at.weuno;

public interface CardActionCompletionContract {
    void onViewMoved(int oldPosition, int newPosition);
    void onViewSwiped(int position);
}
