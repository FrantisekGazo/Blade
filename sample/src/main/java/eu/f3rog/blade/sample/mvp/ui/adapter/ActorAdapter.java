package eu.f3rog.blade.sample.mvp.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.model.Actor;


public final class ActorAdapter
        extends RecyclerView.Adapter<ActorViewHolder> {

    public interface OnActorClickListener {
        void onClick(@NonNull final Actor actor);
    }

    @NonNull
    private final Context mContext;
    @NonNull
    private final List<Actor> mActors;
    @NonNull
    private final OnActorClickListener mListener;

    public ActorAdapter(@NonNull final Context context,
                        @NonNull final List<Actor> actors,
                        @NonNull final OnActorClickListener listener) {
        mContext = context;
        mActors = actors;
        mListener = listener;
    }

    @Override
    public ActorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.mvp_item_actor, parent, false);
        return new ActorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ActorViewHolder holder, int position) {
        holder.bind(mActors.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mActors.size();
    }
}
