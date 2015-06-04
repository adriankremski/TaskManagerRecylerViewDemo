package com.github.snuffix.recyclerviewdemoapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.recycler_view_holder)
    public FrameLayout recyclerViewHolder;

    @InjectView(R.id.toolbar)
    public Toolbar toolbar;

    private RecyclerView recyclerView;

    private List<Task> tasks = new LinkedList<Task>();

    private TaskAdapter taskAdapter;

    private static final Bus BUS = new Bus();

    private SwipeToDismissTouchListener swipeToDismissTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter = new TaskAdapter());

        swipeToDismissTouchListener = new SwipeToDismissTouchListener(recyclerView, new SwipeToDismissTouchListener.DismissCallbacks() {
            @Override
            public SwipeToDismissTouchListener.SwipeDirection canDismiss(int position) {
                return SwipeToDismissTouchListener.SwipeDirection.RIGHT;
            }
            @Override
            public void onDismiss(RecyclerView view, List<SwipeToDismissTouchListener.PendingDismissData> dismissData) {
                for (SwipeToDismissTouchListener.PendingDismissData data : dismissData) {
                    tasks.remove(data.position);
                    taskAdapter.notifyItemRemoved(data.position);
                }
            }
        });
        recyclerView.addOnItemTouchListener(swipeToDismissTouchListener);
        recyclerView.setItemAnimator(new SlideInLeftAnimator());
        recyclerViewHolder.addView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BUS.register(taskAdapter);
    }

    @OnClick(R.id.add_task)
    public void addTask() {
        final EditText input = new EditText(this);

        new AlertDialog.Builder(this).setTitle("New task").setMessage("Please supply task name")
                .setView(input).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                tasks.add(new Task(input.getText().toString()));
                taskAdapter.notifyItemInserted(tasks.size()-1);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                taskAdapter.deleteCheckedItems();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        BUS.unregister(taskAdapter);
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskRowHolder> {

        private Set<Task> selectedTasks = new HashSet<Task>();

        @Override
        public TaskRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TaskRowHolder(getLayoutInflater().inflate(R.layout.view_task_row, parent, false));
        }

        @Override
        public void onBindViewHolder(TaskRowHolder holder, int position) {
            Task task = tasks.get(position);
            holder.setTask(tasks.get(position));
            holder.setChecked(selectedTasks.contains(task));
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        @Subscribe
        public void onTaskCheckStateChangedEvent(TaskCheckStateChangedEvent event){
            if (event.isChecked) {
                selectedTasks.add(event.task);
            } else {
                selectedTasks.remove(event.task);
            }
        }

        public void deleteCheckedItems() {
            for (Task task : selectedTasks) {
                taskAdapter.notifyItemRemoved(tasks.indexOf(task));
                tasks.remove(task);
            }
            selectedTasks.clear();
        }
    }

    class TaskRowHolder extends RecyclerView.ViewHolder {

        private TextView taskNameLabel;
        private CheckBox checkBox;

        private Task task;

        public TaskRowHolder(View itemView) {
            super(itemView);
            taskNameLabel = ButterKnife.findById(itemView, R.id.task_name);
            checkBox = ButterKnife.findById(itemView, R.id.checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    BUS.post(new TaskCheckStateChangedEvent(task, isChecked));
                }
            });
        }

        public void setTask(Task task) {
            this.task = task;
            taskNameLabel.setText(task.name);
        }

        public void setChecked(boolean checked) {
            checkBox.setChecked(checked);
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

    class Task {
        String name;

        public Task(String name) {
            this.name = name;
        }
    }

    public class TaskCheckStateChangedEvent {
        public boolean isChecked;
        public Task task;

        public TaskCheckStateChangedEvent(Task task, boolean isChecked) {
            this.task = task;
            this.isChecked = isChecked;
        }
    }
}
