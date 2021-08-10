package com.kkostrov.avsyncissuedemo;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class AlternativeRenderersFactory extends DefaultRenderersFactory {

    private @MediaCodecRenderer.MediaCodecOperationMode int videoMediaCodecOperationMode;

    private static final String TAG = "AlternativeRenderersFactory";

    @Override
    protected void buildVideoRenderers(Context context, int extensionRendererMode, MediaCodecSelector mediaCodecSelector, boolean enableDecoderFallback, Handler eventHandler, VideoRendererEventListener eventListener, long allowedVideoJoiningTimeMs, ArrayList<Renderer> out) {

        // Use alternative class, which uses workaround - MediaCodecVideoRendererChanged
        MediaCodecVideoRendererChanged videoRenderer =
                new MediaCodecVideoRendererChanged(
                        context,
                        mediaCodecSelector,
                        allowedVideoJoiningTimeMs,
                        enableDecoderFallback,
                        eventHandler,
                        eventListener,
                        MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
        videoRenderer.experimentalSetMediaCodecOperationMode(videoMediaCodecOperationMode);
        out.add(videoRenderer);

        if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
            return;
        }
        int extensionRendererIndex = out.size();
        if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
            extensionRendererIndex--;
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            // LINT.IfChange
            Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            long.class,
                            Handler.class,
                            com.google.android.exoplayer2.video.VideoRendererEventListener.class,
                            int.class);
            // LINT.ThenChange(../../../../../../../proguard-rules.txt)
            Renderer renderer =
                    (Renderer)
                            constructor.newInstance(
                                    allowedVideoJoiningTimeMs,
                                    eventHandler,
                                    eventListener,
                                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
            out.add(extensionRendererIndex++, renderer);
            Log.i(TAG, "Loaded LibvpxVideoRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating VP9 extension", e);
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            // LINT.IfChange
            Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.av1.Libgav1VideoRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            long.class,
                            Handler.class,
                            com.google.android.exoplayer2.video.VideoRendererEventListener.class,
                            int.class);
            // LINT.ThenChange(../../../../../../../proguard-rules.txt)
            Renderer renderer =
                    (Renderer)
                            constructor.newInstance(
                                    allowedVideoJoiningTimeMs,
                                    eventHandler,
                                    eventListener,
                                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
            out.add(extensionRendererIndex++, renderer);
            Log.i(TAG, "Loaded Libgav1VideoRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating AV1 extension", e);
        }
    }

    @Override
    public DefaultRenderersFactory experimentalSetVideoMediaCodecOperationMode(int mode) {
        videoMediaCodecOperationMode = mode;
        return super.experimentalSetVideoMediaCodecOperationMode(mode);
    }

    public AlternativeRenderersFactory(Context context) {
        super(context);
        videoMediaCodecOperationMode = MediaCodecRenderer.OPERATION_MODE_SYNCHRONOUS;
    }
}
