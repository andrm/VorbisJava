/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gagravarr.opus;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggStreamAudioData;

/**
 * Raw, compressed audio data
 */
public class OpusAudioData extends OggStreamAudioData implements OpusPacket {
    
    private int frames = -1;
    private int samples = -1;
    
    public OpusAudioData(OggPacket pkt) {
        super(pkt);
    }
    public OpusAudioData(byte[] data) {
        super(data);
    }

    protected boolean isEndOfStream() {
        return getOggPacket().isEndOfStream();
    }
    public int getFrames() {
        if (frames == -1) {
            getStructure();
        }
        return frames;
    }
    public int getSamples() {
        if (samples == -1) {
            getStructure();
        }
        return samples;
    }
    
    private void getStructure() {
        byte[] d = getData();
        frames = packet_get_nb_frames(d);
        samples = frames * packet_get_samples_per_frame(d, 48000);
    }
    
    private static int packet_get_samples_per_frame(byte[] data, int Fs) {
        int audiosize;
        if ((data[0]&0x80) != 0)
        {
            audiosize = ((data[0]>>3)&0x3);
            audiosize = (Fs<<audiosize)/400;
        } else if ((data[0]&0x60) == 0x60)
        {
            audiosize = ((data[0]&0x08) != 0) ? Fs/50 : Fs/100;
        } else {
            audiosize = ((data[0]>>3)&0x3);
            if (audiosize == 3)
                audiosize = Fs*60/1000;
            else
                audiosize = (Fs<<audiosize)/100;
        }
        return audiosize;

    }

    private static int packet_get_nb_frames(byte[] packet) {
        int count = 0;
        if (packet.length < 1) {
            return -1;
        }
        count = packet[0]&0x3;
        if (count==0)
            return 1;
        else if (count!=3)
            return 2;
        else if (packet.length<2)
            return -4;
        else
            return packet[1]&0x3F;
    }

}
