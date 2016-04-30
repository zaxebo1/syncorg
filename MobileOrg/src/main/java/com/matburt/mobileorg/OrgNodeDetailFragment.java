package com.matburt.mobileorg;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.matburt.mobileorg.OrgData.OrgContract;
import com.matburt.mobileorg.OrgData.OrgFileParser;
import com.matburt.mobileorg.OrgData.OrgNode;
import com.matburt.mobileorg.OrgData.OrgNodeTree;
import com.matburt.mobileorg.util.OrgNodeNotFoundException;
import com.matburt.mobileorg.util.PreferenceUtils;
import com.matburt.mobileorg.util.TodoDialog;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A fragment representing a single OrgNode detail screen.
 * This fragment is either contained in a {@link OrgNodeListActivity}
 * in two-pane mode (on tablets) or a {@link OrgNodeDetailActivity}
 * on handsets.
 */
public class OrgNodeDetailFragment extends Fragment {
    public static String NODE_ID = "node_id";
    public static String PARENT_ID = "parent_id";

    private ContentResolver resolver;

    private long nodeId;
    private long lastEditedPosition;
    private long idHlightedPosition;

    private int gray, red, green, yellow, blue, foreground, foregroundDark, black;
    private static int nTitleColors = 3;
    private int[] titleColor;
    private int[] titleFontSize;
    RecyclerViewAdapter adapter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrgNodeDetailFragment() {
    }

    public String sayHello(){
        return "fragment id : "+idHlightedPosition;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastEditedPosition = -1;
        idHlightedPosition = -1;

        this.resolver = getActivity().getContentResolver();

        gray = ContextCompat.getColor(getContext(), R.color.colorGray);
        red = ContextCompat.getColor(getContext(), R.color.colorRed);
        green = ContextCompat.getColor(getContext(), R.color.colorGreen);
        yellow = ContextCompat.getColor(getContext(), R.color.colorYellow);
        blue = ContextCompat.getColor(getContext(), R.color.colorBlue);
        foreground = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        foregroundDark = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
        black = ContextCompat.getColor(getContext(), R.color.colorBlack);

        titleColor = new int[nTitleColors];
        titleColor[0] = foregroundDark;
        titleColor[1] = foreground;
        titleColor[2] = black;

        titleFontSize = new int[nTitleColors];
        titleFontSize[0] = 25;
        titleFontSize[1] = 20;
        titleFontSize[2] = 16;

        OrgNodeTree tree = null;

        if (getArguments().containsKey(NODE_ID)) {
            this.nodeId = getArguments().getLong(NODE_ID);
            try {
                tree = new OrgNodeTree(new OrgNode(nodeId, resolver), resolver);
            } catch (OrgNodeNotFoundException e) {
//                displayError();
//                TODO: implement error
            }

            Activity activity = this.getActivity();
//            AppBarLayout appBarLayout = (AppBarLayout) activity.findViewById(R.id.app_bar);
//            if (appBarLayout != null && tree != null) {
//                appBarLayout.setTitle(tree.node.getCleanedName());
//            }
        }
        adapter = new RecyclerViewAdapter(tree);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.v("context", "onContextItemSelected");
        return super.onContextItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.node_summary_recycler_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView)rootView.findViewById(R.id.node_recycler_view);
        assert recyclerView != null;
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        registerForContextMenu(recyclerView);

        class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
            private final RecyclerViewAdapter mAdapter;
            public SimpleItemTouchHelperCallback(RecyclerViewAdapter adapter) {
                mAdapter = adapter;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long position = (long)viewHolder.getAdapterPosition();
                OrgNode node = mAdapter.idTreeMap.get(position).node;
                int id = (int)node.id;
                int parentId = (int)node.parentId;
                lastEditedPosition = position;

                createEditNodeFragment(id, parentId, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }
        };

        SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v("newNode", "last : " + lastEditedPosition);
        if(lastEditedPosition > -1) {
            try {
                adapter.tree = new OrgNodeTree(new OrgNode(nodeId, resolver), resolver);
            } catch (OrgNodeNotFoundException e) {
//                displayError();
//                TODO: implement error
            }

            lastEditedPosition = -1;
            adapter.closeInsertItem();
        }
    }

    void createEditNodeFragment(int id, int parentId, int siblingPosition) {
        Bundle args = new Bundle();
        args.putLong(NODE_ID, id);
        args.putLong(PARENT_ID, parentId);
        args.putInt(OrgContract.OrgData.POSITION, siblingPosition);

        Intent intent = new Intent(getActivity(), EditNodeActivity.class);
        intent.putExtras(args);
        startActivity(intent);
    }

    public class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private NavigableMap<Long, OrgNodeTree> idTreeMap;
        private OrgNodeTree tree;

        public RecyclerViewAdapter(OrgNodeTree root) {
            tree = root;
            refresh();
        }


        void refresh(){
            idTreeMap = tree.getVisibleNodesArray();
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch(viewType){
                case 0:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.detail_recycler_item, parent, false);
                    return new ViewHolder(view);
                case 1:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.insert_node_before, parent, false);
                    return new InsertNodeViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder _holder, final int position) {
            if(idHlightedPosition> -1 && ( position==idHlightedPosition+1)){
                final InsertNodeViewHolder holder = (InsertNodeViewHolder) _holder;

                holder.sameLevel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OrgNode currentNode = idTreeMap.get(idHlightedPosition).node;
                        int parentId = (int)currentNode.parentId;
                        lastEditedPosition = position;

                        // Place the node right after this one in the adapter
                        int siblingPosition = currentNode.position + 1;
                        createEditNodeFragment(-1, parentId, siblingPosition);
                    }
                });

                holder.childLevel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int parentId = (int) idTreeMap.get(idHlightedPosition).node.id;
                        lastEditedPosition = position;
                        createEditNodeFragment(-1, parentId, 0);
                    }
                });
            } else {
                final ViewHolder holder = (ViewHolder) _holder;
                holder.mItem = idTreeMap.get((long) position);
                holder.level = holder.mItem.node.level;
                holder.setup(holder.mItem.node);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(idHlightedPosition>-1){
                            closeInsertItem();
                            return;
                        }

                        holder.mItem.toggleVisibility();
                        refresh();
                    }
                });

                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(idHlightedPosition>-1){
                            idTreeMap = tree.getVisibleNodesArray();
                            if(idHlightedPosition<position) idHlightedPosition = position-1;
                            else idHlightedPosition = position;
                        } else {
                            idHlightedPosition = position;
                        }

                        insertItem((int)idHlightedPosition);
                        notifyDataSetChanged();
                        return false;
                    }
                });
            }
        }

        /**
         * Add an item before position
         * @param position
         */
        private void insertItem(int position){
            NavigableMap<Long, OrgNodeTree> newIdTreeMap = new TreeMap<>();
            long newId = 0, oldId = 0;
            while(oldId<idTreeMap.size()) {
                if(oldId==(long)position+1) newId++;
//                if(oldId==(long)position || oldId==(long)position+1) newId++;
                newIdTreeMap.put(newId++, idTreeMap.get(oldId++));
            }
            idTreeMap = newIdTreeMap;
        }

        private void closeInsertItem(){
            idHlightedPosition = -1;
            refresh();
        }

        @Override
        public int getItemCount() {
            int count = idTreeMap.size();
            if(idHlightedPosition>-1) count++;
            return count;
        }

        @Override
        public int getItemViewType(int position) {
            if(idHlightedPosition>-1) {
//                if (position == idHlightedPosition) return 1;
//                if (position == idHlightedPosition + 2) return 1;
                if (position == idHlightedPosition + 1) return 1;
            }
            return 0;
        }

        public class InsertNodeViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            private Button sameLevel, childLevel;
            public InsertNodeViewHolder(View view) {
                super(view);
                mView = view;
                sameLevel = (Button) view.findViewById(R.id.insert_same_level);
                childLevel = (Button) view.findViewById(R.id.insert_neighbourg_level);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
            public final View mView;

            public OrgNodeTree mItem;
            private TextView titleView;
            private Button todoButton;
            private TextView levelView;
            private boolean levelFormatting = true;
            public long level;

            public ViewHolder(View view) {
                super(view);
                mView = view;

                titleView = (TextView) view.findViewById(R.id.outline_item_title);
                todoButton = (Button) view.findViewById(R.id.outline_item_todo);
                levelView = (TextView) view.findViewById(R.id.outline_item_level);
                todoButton.setOnClickListener(todoClick);

                int fontSize = PreferenceUtils.getFontSize();
                todoButton.setTextSize(fontSize);
                view.setOnCreateContextMenuListener(this);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                Log.v("context", "onCreateContextMenu");
                menu.add(Menu.NONE, R.id.edit_menu_ok,
                        Menu.NONE, "blah");
            }

//            @Override
//            public void onViewRecycled(ViewHolder holder) {
//                holder.itemView.setOnLongClickListener(null);
//                super.onViewRecycled(holder);
//            }

            private View.OnClickListener todoClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(idHlightedPosition>-1){
                        closeInsertItem();
                        return;
                    }
                    new TodoDialog(getContext(),mItem.node, todoButton);
                }
            };


//            @Override
//            public String toString() {
//                return super.toString() + " '" + mContentView.getText() + "'";
//            }


            public void setupPriority(String priority, SpannableStringBuilder titleSpan) {
                if (priority != null && !TextUtils.isEmpty(priority)) {
                    Spannable prioritySpan = new SpannableString(priority + " ");
                    int yellow = ContextCompat.getColor(getContext(), R.color.colorYellow);
                    prioritySpan.setSpan(new ForegroundColorSpan(yellow), 0,
                            priority.length(), 0);
                    titleSpan.insert(0, prioritySpan);
                }
            }

            public void applyLevelIndentation(long level, SpannableStringBuilder item) {
                String indentString = "";
                for(int i = 0; i < level; i++)
                    indentString += "   ";

                this.levelView.setText(indentString);
            }

            public void applyLevelFormating(long level, SpannableStringBuilder item) {
//                item.setSpan(
//                        new ForegroundColorSpan(theme.levelColors[(int) Math
//                                .abs((level) % theme.levelColors.length)]), 0, item
//                                .length(), 0);
            }

            public void setupTitle(String name, SpannableStringBuilder titleSpan) {
                titleView.setGravity(Gravity.LEFT);
                titleView.setTextSize(titleFontSize[Math.min((int)level-1, nTitleColors)]);
                if(level==1) titleView.setTypeface(null, Typeface.BOLD);
                else titleView.setTypeface(null,Typeface.NORMAL);

                if (name.startsWith("COMMENT"))
                    titleSpan.setSpan(new ForegroundColorSpan(gray), 0,
                            "COMMENT".length(), 0);
                else if (name.equals("Archive"))
                    titleSpan.setSpan(new ForegroundColorSpan(gray), 0,
                            "Archive".length(), 0);

                formatLinks(titleSpan);
            }

            public void setupAgendaBlock(SpannableStringBuilder titleSpan) {
                titleSpan.delete(0, OrgFileParser.BLOCK_SEPARATOR_PREFIX.length());

                titleSpan.setSpan(new ForegroundColorSpan(foreground), 0,
                        titleSpan.length(), 0);
                titleSpan.setSpan(new StyleSpan(Typeface.BOLD), 0,
                        titleSpan.length(), 0);

                titleView.setTextSize(PreferenceUtils.getFontSize() + 4);
                //titleView.setBackgroundColor(theme.c4Blue);
                titleView.setGravity(Gravity.CENTER_VERTICAL
                        | Gravity.CENTER_HORIZONTAL);

                titleView.setText(titleSpan);
            }

            public final Pattern urlPattern = Pattern.compile("\\[\\[[^\\]]*\\]\\[([^\\]]*)\\]\\]");
            private void formatLinks(SpannableStringBuilder titleSpan) {
                Matcher matcher = urlPattern.matcher(titleSpan);
                while(matcher.find()) {
                    titleSpan.delete(matcher.start(), matcher.end());
                    titleSpan.insert(matcher.start(), matcher.group(1));

                    titleSpan.setSpan(new ForegroundColorSpan(blue),
                            matcher.start(), matcher.start() + matcher.group(1).length(), 0);

                    matcher = urlPattern.matcher(titleSpan);
                }
            }

            public void setLevelFormating(boolean enabled) {
                this.levelFormatting = enabled;
            }



            public void setup(OrgNode node) {
                this.mItem.node = node;

                SpannableStringBuilder titleSpan = new SpannableStringBuilder(node.name);

                if(node.name.startsWith(OrgFileParser.BLOCK_SEPARATOR_PREFIX)) {
                    setupAgendaBlock(titleSpan);
                    return;
                }

                if (levelFormatting)
                    applyLevelFormating(node.level, titleSpan);
                setupTitle(node.name, titleSpan);
                setupPriority(node.priority, titleSpan);
                TodoDialog.setupTodoButton(getContext(), node, todoButton, true);

                if (levelFormatting)
                    applyLevelIndentation(node.level, titleSpan);

                if(this.mItem.getVisibility()== OrgNodeTree.Visibility.folded)
                    setupChildrenIndicator(node, titleSpan);

//                titleSpan.setSpan(new StyleSpan(Typeface.NORMAL), 0, titleSpan.length(), 0);
                titleView.setText(titleSpan);
                int colorId = (int) Math.min(level-1,nTitleColors-1);
                titleView.setTextColor(titleColor[colorId]);
            }

            public void setupChildrenIndicator(OrgNode node, SpannableStringBuilder titleSpan) {
                if (node.hasChildren(resolver)) {
                    titleSpan.append("...");
                    titleSpan.setSpan(new ForegroundColorSpan(foreground),
                            titleSpan.length() - "...".length(), titleSpan.length(), 0);
                }
            }
        }


    }



    public class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{android.R.attr.listDivider};

        private Drawable mDivider;

        /**
         * Default divider will be used
         */
        public DividerItemDecoration(Context context) {
            final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
            mDivider = styledAttributes.getDrawable(0);
            styledAttributes.recycle();
        }

        /**
         * Custom divider will be used
         */
        public DividerItemDecoration(Context context, int resId) {
            mDivider = ContextCompat.getDrawable(context, resId);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}


