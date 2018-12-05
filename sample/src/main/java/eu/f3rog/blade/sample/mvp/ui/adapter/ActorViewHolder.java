package eu.f3rog.blade.sample.mvp.ui.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.mvp.model.Actor;

/* package */ final class ActorViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    @BindView(android.R.id.text1)
    TextView mName;

    @Nullable
    private Actor mActor;
    @Nullable
    private ActorAdapter.OnActorClickListener mListener;

    public ActorViewHolder(@NonNull final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mName.setOnClickListener(this);
    }

    public void bind(@NonNull final Actor actor, @Nullable final ActorAdapter.OnActorClickListener listener) {
        mActor = actor;
        mListener = listener;

        mName.setText(actor.getName());
    }

    @Override
    public void onClick(View view) {
        if (mActor != null && mListener != null) {
            mListener.onClick(mActor);
        }
    }
}
