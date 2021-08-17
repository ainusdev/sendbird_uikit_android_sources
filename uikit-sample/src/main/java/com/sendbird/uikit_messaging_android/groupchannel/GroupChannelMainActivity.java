package com.sendbird.uikit_messaging_android.groupchannel;

import static com.sendbird.uikit_messaging_android.consts.StringSet.PUSH_REDIRECT_CHANNEL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelTotalUnreadMessageCountParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.fragments.ChannelListFragment;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.SettingsFragment;
import com.sendbird.uikit_messaging_android.databinding.ActivityGroupChannelMainBinding;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.widgets.CustomTabView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupChannelMainActivity extends AppCompatActivity implements OnItemClickListener<GroupChannel> {
    private static final String USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis();
    private static final String TAG = GroupChannelMainActivity.class.getSimpleName();

    private ActivityGroupChannelMainBinding binding;
    private CustomTabView unreadCountTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_channel_main);
        initPage();
    }

    private void initPage() {
        binding.vpMain.setAdapter(new MainAdapter(this, getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));

        boolean isDarkMode = PreferenceUtils.isUsingDarkTheme();
        int backgroundRedId = isDarkMode ? R.color.background_600 : R.color.background_50;
        binding.tlMain.setBackgroundResource(backgroundRedId);
        binding.tlMain.setupWithViewPager(binding.vpMain);

        unreadCountTab = new CustomTabView(this);
        unreadCountTab.setBadgeVisibility(View.GONE);
        unreadCountTab.setTitle(getString(R.string.text_tab_channels));
        unreadCountTab.setIcon(R.drawable.icon_chat_filled);

        CustomTabView settingsTab = new CustomTabView(this);
        settingsTab.setBadgeVisibility(View.GONE);
        settingsTab.setTitle(getString(R.string.text_tab_settings));
        settingsTab.setIcon(R.drawable.icon_settings_filled);

        Objects.requireNonNull(binding.tlMain.getTabAt(0)).setCustomView(unreadCountTab);
        Objects.requireNonNull(binding.tlMain.getTabAt(1)).setCustomView(settingsTab);

        redirectChannelIfNeeded(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SendBird.getTotalUnreadMessageCount(new GroupChannelTotalUnreadMessageCountParams(), (totalCount, e) -> {
            if (e != null) {
                return;
            }

            if (totalCount > 0) {
                unreadCountTab.setBadgeVisibility(View.VISIBLE);
                unreadCountTab.setBadgeCount(totalCount > 99 ?
                        getString(R.string.text_tab_badge_max_count) :
                        String.valueOf(totalCount));
            } else {
                unreadCountTab.setBadgeVisibility(View.GONE);
            }
        });

        SendBird.addUserEventHandler(USER_EVENT_HANDLER_KEY, new SendBird.UserEventHandler() {
            @Override
            public void onFriendsDiscovered(List<User> list) {
            }

            @Override
            public void onTotalUnreadMessageCountChanged(int totalCount, Map<String, Integer> totalCountByCustomType) {
                if (totalCount > 0) {
                    unreadCountTab.setBadgeVisibility(View.VISIBLE);
                    unreadCountTab.setBadgeCount(totalCount > 99 ?
                            getString(R.string.text_tab_badge_max_count) :
                            String.valueOf(totalCount));
                } else {
                    unreadCountTab.setBadgeVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SendBird.removeUserEventHandler(USER_EVENT_HANDLER_KEY);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        redirectChannelIfNeeded(intent);
    }

    public static Intent newRedirectToChannelIntent(@NonNull Context context, @NonNull String channelUrl) {
        Intent intent = new Intent(context, GroupChannelMainActivity.class);
        intent.putExtra(PUSH_REDIRECT_CHANNEL, channelUrl);
        return intent;
    }

    private void redirectChannelIfNeeded(Intent intent) {
        if (intent == null) return;

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            getIntent().removeExtra(PUSH_REDIRECT_CHANNEL);
        }
        if (intent.hasExtra(PUSH_REDIRECT_CHANNEL)) {
            String channelUrl = intent.getStringExtra(PUSH_REDIRECT_CHANNEL);
            startActivity(ChannelActivity.newIntentFromCustomActivity(this, CustomChannelActivity.class, channelUrl));
            intent.removeExtra(PUSH_REDIRECT_CHANNEL);
        }
    }

    @Override
    public void onItemClick(View view, int i, GroupChannel groupChannel) {
        startActivity(ChannelActivity.newIntentFromCustomActivity(this, CustomChannelActivity.class, groupChannel.getUrl()));
    }

    private static class MainAdapter extends FragmentPagerAdapter {
        private static final int PAGE_SIZE = 2;
        private static GroupChannelMainActivity groupChannelMainActivity;

        public MainAdapter(GroupChannelMainActivity groupChannelMainActivity, @NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);

            MainAdapter.groupChannelMainActivity = groupChannelMainActivity;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ChannelListFragment.Builder()
                        .setCustomChannelListFragment(new CustomChannelListFragment())
                        .setUseHeader(true)
                        .setUseHeaderLeftButton(false)
                        .setItemClickListener(groupChannelMainActivity)
                        .build();
            } else {
                return new SettingsFragment();
            }
        }

        @Override
        public int getCount() {
            return PAGE_SIZE;
        }
    }
}
