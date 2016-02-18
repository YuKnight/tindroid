package co.tinode.tindroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.MsgServerData;

/**
 * Created by gsokolov on 2/5/16.
 */
public class MessagesListAdapter extends BaseAdapter {
    private static final String TAG = "MessagesListAdapter";

    // Vertical padding between two messages from different senders
    private static final int SINGLE_PADDING = 10;
    // Vertical padding between two messages from the same sender
    private static final int TRAIN_PADDING = 2;

    // Material colors, shade #200
    private static final colorizer[] sColorizer = {
            new colorizer(0xffef9a9a, 0xff212121), new colorizer(0xffc5e1a5, 0xff212121),
            new colorizer(0xff90caf9, 0xff212121), new colorizer(0xfffff59d, 0xff212121),
            new colorizer(0xffb0bec5, 0xff212121), new colorizer(0xfff48fb1, 0xff212121),
            new colorizer(0xffb39ddb, 0xff212121), new colorizer(0xff9fa8da, 0xff212121),
            new colorizer(0xffffab91, 0xff212121), new colorizer(0xffffe082, 0xff212121),
            new colorizer(0xffa5d6a7, 0xff212121), new colorizer(0xffbcaaa4, 0xff212121),
            new colorizer(0xffeeeeee, 0xff212121), new colorizer(0xff80deea, 0xff212121),
            new colorizer(0xffe6ee9c, 0xff212121), new colorizer(0xffce93d8, 0xff212121)
    };

    private Context mContext;
    private String mTopicName;
    private Topic mTopic;

    public MessagesListAdapter(Context context) {
        mContext = context;
    }

    public void changeTopic(String topicName) {
        if (mTopicName == null || !mTopicName.equals(topicName)) {
            Log.d(TAG, "Topic name has changed from '" + mTopicName + "' to '" + topicName +"'");

            mTopicName = topicName;
            mTopic = InmemoryCache.getTinode().getTopic(topicName);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mTopic.getMessageCount();
    }

    @Override
    public Object getItem(int position) {
        return mTopic.getMessageAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressWarnings("unchecked")
        MsgServerData<String> m = mTopic.getMessageAt(position);
        int senderIdx = mTopic.getSenderIndex(m.from);
        if (senderIdx < 0) {
            senderIdx = 0;
        }

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.message, null);
        }

        LinearLayout container = (LinearLayout) convertView.findViewById(R.id.container);
        container.setGravity(m.isMine ? Gravity.RIGHT : Gravity.LEFT);
        // First set background, then set text.
        View bubble = convertView.findViewById(R.id.messageBubble);
        int bg_bubble = m.isMine ? R.drawable.bubble_r : R.drawable.bubble_l;
        switch (m.getDisplay()) {
            case SINGLE:
                bg_bubble = m.isMine ? R.drawable.bubble_r : R.drawable.bubble_l;
                container.setPadding(container.getPaddingLeft(), SINGLE_PADDING,
                        container.getPaddingRight(), SINGLE_PADDING);
                break;
            case FIRST:
                bg_bubble = m.isMine ? R.drawable.bubble_r_z : R.drawable.bubble_l_z;
                container.setPadding(container.getPaddingLeft(), SINGLE_PADDING,
                        container.getPaddingRight(), TRAIN_PADDING);
                break;
            case MIDDLE:
                bg_bubble = m.isMine ? R.drawable.bubble_r_z : R.drawable.bubble_l_z;
                container.setPadding(container.getPaddingLeft(), TRAIN_PADDING,
                        container.getPaddingRight(), TRAIN_PADDING);
                break;
            case LAST:
                bg_bubble = m.isMine ? R.drawable.bubble_r : R.drawable.bubble_l;
                container.setPadding(container.getPaddingLeft(), TRAIN_PADDING,
                        container.getPaddingRight(), SINGLE_PADDING);
                break;
        }
        bubble.setBackgroundResource(bg_bubble);
        if (!m.isMine) {
            bubble.getBackground().mutate()
                    .setColorFilter(sColorizer[senderIdx].bg, PorterDuff.Mode.MULTIPLY);
        }
        ((TextView) convertView.findViewById(R.id.messageText)).setText(m.content);
        ((TextView) convertView.findViewById(R.id.messageMeta)).setText(shortDate(m.ts));

        ImageView delivered = (ImageView) convertView.findViewById(R.id.messageViewedIcon);
        delivered.setImageResource(android.R.color.transparent);
        if (m.isMine) {
            if (mTopic.msgReadCount(m.seq) > 0) {
                delivered.setImageResource(R.drawable.ic_done_all);
            } else if (mTopic.msgRecvCount(m.seq) > 0) {
                delivered.setImageResource(R.drawable.ic_done);
            }
        }
        return convertView;
    }

    static class colorizer {
        public int bg;
        public int fg;

        public colorizer(int bg, int fg) {
            this.bg = bg;
            this.fg = fg;
        }
    }

    public static String shortDate(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.setTime(date);

        if (then.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            if (then.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                    then.get(Calendar.DATE) == now.get(Calendar.DATE)) {
                return DateFormat.getTimeInstance(DateFormat.SHORT).format(then.getTime());
            } else {
                return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(then.getTime());
            }
        }
        return DateFormat.getInstance().format(then.getTime());
    }
}