package com.github.snuffix.recyclerviewdemoapp;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.recycler_view_holder)
    public FrameLayout recyclerViewHolder;

    private RecyclerView recyclerView;

    private List<String> tasks = new LinkedList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        initTasks();

        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TaskAdapter());
        recyclerView.addItemDecoration(new TaskRowItemDecoration(getDrawable(R.drawable.task_divider)));
        recyclerViewHolder.addView(recyclerView);
    }

    private void initTasks() {
        tasks.add("Wake up");
        tasks.add("Go to work");
        tasks.add("Make a coffee");
        tasks.add("Go to standup");
        tasks.add("Make a coffee");
        tasks.add("Spend some time in chillout room");
        tasks.add("Go home");
        tasks.add("Sleep");
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskRowHolder> {

        @Override
        public TaskRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TaskRowHolder(getLayoutInflater().inflate(R.layout.view_task_row, parent, false));
        }

        @Override
        public void onBindViewHolder(TaskRowHolder holder, int position) {
            holder.setTask(tasks.get(position));
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }
    }

    class TaskRowHolder extends RecyclerView.ViewHolder {

        private TextView taskNameLabel;

        private String task;

        public TaskRowHolder(View itemView) {
            super(itemView);
            taskNameLabel = ButterKnife.findById(itemView, R.id.task_name);
        }

        public void setTask(String task) {
            this.task = task;
            taskNameLabel.setText(task);
        }
    }

    public class TaskRowItemDecoration extends RecyclerView.ItemDecoration {

        private Drawable dividerDrawable;

        public TaskRowItemDecoration(Drawable drawable) {
            this.dividerDrawable = drawable.mutate();
        }

        @Override
        public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + dividerDrawable.getIntrinsicHeight();

                dividerDrawable.setBounds(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
                dividerDrawable.draw(canvas);
            }
        }
    }
}
