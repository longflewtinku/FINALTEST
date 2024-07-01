package com.linkly.launcher.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.launcher.BR;
import com.linkly.launcher.R;
import com.linkly.launcher.databinding.FragmentUnattendedIdleScreenBinding;
import com.linkly.launcher.viewmodels.UnattendedIdleScreenViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UnattendedIdleScreenFragment} factory method to
 * create an instance of this fragment.
 */
public class UnattendedIdleScreenFragment extends Fragment {
    private UnattendedIdleScreenViewModel viewModel;
    private FragmentUnattendedIdleScreenBinding binding;

    public static UnattendedIdleScreenFragment newInstance() {
        return new UnattendedIdleScreenFragment();
    }

    public UnattendedIdleScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UnattendedIdleScreenViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate( inflater, R.layout.fragment_unattended_idle_screen, container, false );
        binding.setLifecycleOwner(getViewLifecycleOwner());
        View view = binding.getRoot();
        binding.setViewModel(viewModel);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setVariable(BR.viewModel, viewModel);
        binding.executePendingBindings();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
