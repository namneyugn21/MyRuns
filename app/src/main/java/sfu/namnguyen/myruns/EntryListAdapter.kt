package sfu.namnguyen.myruns

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class EntryListAdapter(private val clickListener: (ExerciseEntry) -> Unit) :
    ListAdapter<ExerciseEntry, EntryListAdapter.EntryViewHolder>(EntryDiff) {
        var isMetric = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry, isMetric)

        holder.itemView.setOnClickListener {
            clickListener(entry)
        }
    }

    class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mainHeaderTextView: TextView = itemView.findViewById(R.id.text_main_header)
        private val subHeaderTextView: TextView = itemView.findViewById(R.id.text_sub_header)
        private val distanceTextView: TextView = itemView.findViewById(R.id.text_distance)
        private val durationTextView: TextView = itemView.findViewById(R.id.text_duration)


        @SuppressLint("SetTextI18n")
        fun bind(entry: ExerciseEntry, isMetric: Boolean) {
            // get readable names
            val activityName = ActivityTypeConverter.toName(entry.activityType)
            val inputName = ActivityTypeConverter.inputTypeToName(entry.inputType)

            // format Date and Time
            val dateTimeString = UnitConverter.formatDateTime(entry.dateTime)

            // format Distance and Duration
            val distanceFormatted = UnitConverter.formatDistance(entry.distance, isMetric)
            val durationFormatted = UnitConverter.formatDuration(entry.duration)

            // set the TextViews
            mainHeaderTextView.text = "$inputName: $activityName"
            subHeaderTextView.text = dateTimeString
            distanceTextView.text = "Distance: $distanceFormatted"
            durationTextView.text = "Duration: $durationFormatted"
        }
    }
}

object EntryDiff : DiffUtil.ItemCallback<ExerciseEntry>() {
    override fun areItemsTheSame(oldItem: ExerciseEntry, newItem: ExerciseEntry): Boolean {
        return oldItem.id == newItem.id
    }

    // checks if any content field has changed
    override fun areContentsTheSame(oldItem: ExerciseEntry, newItem: ExerciseEntry): Boolean {
        return oldItem == newItem
    }
}