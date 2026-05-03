SpecBuf {
	var <server, <numChannels;
	var <file, <fftSize, <overlaps, <analysis, <filepath;

	*new { |server, file, fftSize, overlaps = 2, numChannels = 1|
		^super.new.init(server, file, fftSize, overlaps, numChannels)
	}

	init { |srv, argFile, argFftSize, argOverlaps, nChans|
		server      = srv;
		numChannels = nChans;
		filepath    = argFile;
		fftSize     = argFftSize;
		overlaps    = argOverlaps;

		(numChannels == 1).if({
			file     = Buffer.readChannel(server, argFile, channels: [0]);
			analysis = Array.fill(argOverlaps, { Buffer.alloc(server, argFftSize) });
		}, {
			file = Array.fill(numChannels, { |ch|
				Buffer.readChannel(server, argFile, channels: [ch])
			});
			analysis = Array.fill(numChannels, { |ch|
				Array.fill(argOverlaps, { Buffer.alloc(server, argFftSize) })
			});
		});
	}

	analysisBufs {
		^analysis.flat;
	}

	analysisForChannel { |ch = 0|
		(numChannels == 1).if({ ^analysis }, { ^analysis[ch] })
	}

	fileForChannel { |ch = 0|
		(numChannels == 1).if({ ^file }, { ^file[ch] })
	}

	free {
		file !? { |b|
			b.isKindOf(Array).if({ b.do(_.free) }, { b.free })
		};
		analysis !? { |bufs|
			bufs.isKindOf(Array).if({
				bufs[0].isKindOf(Array).if(
					{ bufs.do { |chanBufs| chanBufs.do(_.free) } },
					{ bufs.do(_.free) }
				)
			});
		};
		file     = nil;
		analysis = nil;
		filepath = nil;
		fftSize  = nil;
		overlaps = nil;
	}

	printOn { |stream|
		stream << "SpecBuf(channels: %, fftSize: %, overlaps: %)".format(
			numChannels, fftSize, overlaps)
	}
}