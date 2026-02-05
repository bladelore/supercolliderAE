(
//server options
s.options.blockSize = 4;
s.options.hardwareBufferSize = 2048;
s.options.memSize = 8192 * 16;
s.options.numWireBufs = 8192*2048;

//remember to change clock
t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

s.waitForBoot {
	var startupPath = "/Users/aelazary/sc/sc_music_old/startup";
	var instrumentsPath = startupPath ++ "/instruments";

	(startupPath ++ "/noiseFuncs.scd").load;
	(startupPath ++ "/patternFuncs.scd").load;
	(startupPath ++ "/flucomaFuncs.scd").load;

	(startupPath ++ "/FX.scd").load;
	(startupPath ++ "/spectral.scd").load;

	(instrumentsPath ++ "/droneSynths.scd").load;
	(instrumentsPath ++ "/fmSynths.scd").load;
	(instrumentsPath ++ "/microsoundSynths.scd").load;
	(instrumentsPath ++ "/percSynths.scd").load;
	(instrumentsPath ++ "/physmodSynths.scd").load;
	(instrumentsPath ++ "/slicerSynths.scd").load;
};
)
