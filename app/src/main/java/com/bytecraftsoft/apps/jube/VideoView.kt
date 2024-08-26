import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView

class VideoAdapter(private val videoUrls: List<String>, private val recyclerView: RecyclerView) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    companion object {
        val activePlayers = mutableListOf<ExoPlayer>()
    }

    private var currentPlayingIndex: Int = -1

    class VideoViewHolder(val playerView: PlayerView) : RecyclerView.ViewHolder(playerView) {
        var player: ExoPlayer? = null

        fun bind(videoUrl: String) {
            if (player == null) {
                player = ExoPlayer.Builder(playerView.context).build().apply {
                    VideoAdapter.activePlayers.add(this)
                }
                playerView.player = player
            }
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            player?.setMediaItem(mediaItem)
            player?.prepare()
            Log.d("VideoAdapter", "Binding video: $videoUrl")
        }

        fun releasePlayer() {
            Log.d("VideoAdapter", "Releasing player")
            player?.release()
            VideoAdapter.activePlayers.remove(player)
            player = null
        }

        fun play() {
            player?.play()
        }

        fun pause() {
            player?.pause()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val playerView = PlayerView(parent.context)
        playerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return VideoViewHolder(playerView)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoUrls[position])
        if (position == currentPlayingIndex) {
            holder.play()
        } else {
            holder.pause()
        }
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    override fun getItemCount(): Int = videoUrls.size

    fun pauseCurrentVideo() {
        if (currentPlayingIndex != -1) {
            val holder = recyclerView.findViewHolderForAdapterPosition(currentPlayingIndex) as? VideoViewHolder
            holder?.pause()
        }
    }

    fun playVideoAt(index: Int) {
        if (currentPlayingIndex == index) return
        pauseCurrentVideo()
        currentPlayingIndex = index
        val holder = recyclerView.findViewHolderForAdapterPosition(currentPlayingIndex) as? VideoViewHolder
        holder?.play()
    }

    fun checkCurrentVideoPlaying() {
        val firstVisibleItemPosition = (recyclerView.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)?.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = (recyclerView.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)?.findLastVisibleItemPosition()

        if (firstVisibleItemPosition != null && lastVisibleItemPosition != null) {
            for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                val holder = recyclerView.findViewHolderForAdapterPosition(i) as? VideoViewHolder
                if (holder != null && isViewVisible(holder.itemView, recyclerView.context)) {
                    playVideoAt(i)
                    //break
                }
            }
        }
    }

    private fun isViewVisible(view: View, context: Context): Boolean {
        val itemRect = Rect()
        view.getGlobalVisibleRect(itemRect)
        val screenHeight = context.resources.displayMetrics.heightPixels
        return itemRect.bottom > 100 && itemRect.top < screenHeight - 50
    }


}
