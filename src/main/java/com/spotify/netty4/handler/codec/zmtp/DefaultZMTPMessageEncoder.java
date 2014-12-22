/*
 * Copyright (c) 2012-2014 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.spotify.netty4.handler.codec.zmtp;

import java.util.List;

import io.netty.buffer.ByteBuf;

public class DefaultZMTPMessageEncoder implements ZMTPMessageEncoder {

  @Override
  public void encode(final Object msg, final ZMTPWriter writer) {
    final ZMTPMessage message = (ZMTPMessage) msg;

    final List<ZMTPFrame> envelope = message.envelope();
    final List<ZMTPFrame> content = message.content();

    for (final ZMTPFrame frame : envelope) {
      writer.expectEnvelope(frame.size());
    }

    for (final ZMTPFrame frame : content) {
      writer.expectContent(frame.size());
    }

    writer.beginEnvelopes();

    for (int i = 0; i < envelope.size(); i++) {
      final ZMTPFrame frame = envelope.get(i);
      final ByteBuf dst = writer.envelope(frame.size());
      final ByteBuf src = frame.content();
      dst.writeBytes(src, src.readerIndex(), src.readableBytes());
    }

    writer.endEnvelopes();

    writer.beginContents();

    for (int i = 0; i < content.size(); i++) {
      final ZMTPFrame frame = content.get(i);
      final ByteBuf dst = writer.content(frame.size());
      final ByteBuf src = frame.content();
      dst.writeBytes(src, src.readerIndex(), src.readableBytes());
    }

    writer.endContents();
  }
}