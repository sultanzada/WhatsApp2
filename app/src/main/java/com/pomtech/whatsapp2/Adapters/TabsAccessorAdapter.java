package com.pomtech.whatsapp2.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.pomtech.whatsapp2.Fragments.ChatsFragment;
import com.pomtech.whatsapp2.Fragments.ContactsFragment;
import com.pomtech.whatsapp2.Fragments.GroupsFragment;
import com.pomtech.whatsapp2.Fragments.RequestsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 2:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
            case 3:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Requests";
            case 2:
                return "Groups";
            case 3:
                return "Contacts";
            default:
                return null;
        }

    }
}
