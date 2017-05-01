package com.lowlightstudios.watlo.Custom;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lowlightstudios.watlo.R;
import com.lowlightstudios.watlo.models.InfoCard;

import java.util.ArrayList;
import java.util.List;

public class InfoCardsAdapter extends RecyclerView.Adapter<InfoCardsAdapter.InfoCardsViewHolder> {
    private LayoutInflater inflater;
    private List<InfoCard> infoCardsData = new ArrayList<>();

    public InfoCardsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.infoCardsData = new ArrayList<>();
    }

    public void setInfoCardsData(List<InfoCard> infoCardsData) {
        this.infoCardsData = infoCardsData;
    }

    public void addCard(InfoCard infoCard) {
        this.infoCardsData.add(infoCard);
    }

    public void clean() {
        this.infoCardsData.clear();
    }

    @Override
    public InfoCardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.sliding_cards, parent, false);
        return new InfoCardsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InfoCardsViewHolder holder, int position) {
        InfoCard currentCard = infoCardsData.get(position);
        holder.title.setText(currentCard.getTitle());
    }

    @Override
    public int getItemCount() {
        return this.infoCardsData.size();
    }

    class InfoCardsViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public InfoCardsViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.card_title);
        }
    }
}
