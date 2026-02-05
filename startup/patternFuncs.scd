///COMPOSTION FUNCTIONS
(
//to do
//metric modulation
//accel/decel by phase?
//swing/swing by group
//choke groups
//add risers to hits
//sequence sections
//delta to

~appendRest = { |pattern, pDur, restDur|
	Pseq([pattern.finDur(pDur), Rest(restDur)], inf);
};

~pFade = { |a, b, dur, curve='linear', loop=0|
	var env;
	var chance = Pwhite(0.0, 1.0, inf).asStream;

	if(loop == 1)
	{ env = Pn(Pseg(Pseq([0 + 1e-4, 1, 0 + 1e-4], 1), dur, curve, inf)).asStream; }
	{ env = Env.new([0 + 1e-6, 1], dur, curve).asStream; };

	Ppar([
		Pbindf(a, \dur,
			Pfunc({|ev|
				var val;
				if((ev[\type] == 'monoNote').or(ev[\type] == 'monoSet'))
				{
					val = ev[\dur];
					ev[\amp] = 1 - env.next;
				} {
					if(chance.next < env.next)
					// { val = \rest; }
					// { val = ev[\instrument]};
					{val = Rest(ev[\dur])}{val = ev[\dur]}
				};
				val;
			})
		),

		Pbindf(b, \dur,
			Pfunc({|ev|
				var val;
				if((ev[\type] == 'monoNote').or(ev[\type] == 'monoSet'))
				{
					val = ev[\dur];
					ev[\amp] = env.next;
				} {
					if(chance.next > env.next)
					// { val = \rest; }
					// { val = ev[\instrument]};
					{val = Rest(ev[\dur])}{val = ev[\dur]}
				};
				val;
			})
		)
	]);
};

~subPatternDepth = { |array|
    var depths, size;
	array = array.list;
    if (array.isKindOf(Array)) {
        depths = array.collect { |elem, i|
            if (elem.class == Ppatlace) {
                ~subPatternDepth.value(elem) + 1;
            } {
                1;
            }
        };
		depths.maxItem;
    } {
        0;
    };
};

~subPatternSize = { |pattern|
	var size;
	pattern = pattern.list;
	size = pattern.size;
	pattern.do { |elem, i|
		if (elem.class == Ppatlace) {
			size = ~subPatternSize.value(elem) * size;
		}
	};

	size;
};

~makeSubdivision = { |patt, subdiv|
	var mutePattern, pattSize, subdivSize, pattDepth, numEvents, lcmEvents, groupcycleLCM;
	//get size of laced duration pattern
	pattSize = ~subPatternSize.(patt);
	//get size of laced subdiv pattern
	subdivSize = ~subPatternSize.(subdiv);
	//get the max depth of the pattern
	pattDepth = ~subPatternDepth.(patt);
	// pattSize.postln;
	// subdivSize.postln;
	// pattDepth.postln;
	//allow for zeroes in subdiv pattern to set the event to rest
	mutePattern = subdiv;
	subdiv = Pcollect({|item| if(item == 0){ 1 }{ item }}, subdiv);
	//calculate the number of events in the cycle
	numEvents = subdiv.asStream.nextN(subdivSize).sum * pattDepth;
	// numEvents.postln;
	//create the stream of mutes
	mutePattern = Pdup(subdiv, mutePattern);
	//create the stream of subdivisions
	subdiv = Pdup(subdiv, subdiv);
	if((pattDepth) == subdivSize){ lcmEvents = numEvents}{ lcmEvents = numEvents * pattSize};
	// groupcycleLCM = lcm(pattSize, subdivSize);
	groupcycleLCM=pattSize*subdivSize;
	// groupcycleLCM.postln;
	//calculate delta values from subdivision pattern
	Plazy{ ~makeDeltas.() } <>
	//mute if 0 on pattern
	Plazy{ Pfunc({|event| if(event[\mutepattern] == 0)
		// { event[\instrument] = \rest }
		// { event[\instrument] = event[\instrument]}; }
		{ event[\dur] = Rest(event[\dur])}
		{ event[\dur] = event[\dur]}; }
	)} <>
	Pbind(
		//embed data into pattern for ~makeDelta function
		\subdiv, Pn(subdiv , inf),
		\dur, Psubdivide(Pkey(\subdiv), Pn(patt, inf)),
		//wrapped events, unwraps at bracketed only
		\groupcycle, Pn(pattSize, inf), 
		\subdivSize, Pn(subdivSize, inf),
		\numevents, Pn(numEvents, inf),
		\mutepattern, Pn(mutePattern, inf),
		//all unwrapped events
		\groupcycleLCM, Pn(groupcycleLCM, inf),
		\numeventsLCM, Pn(lcmEvents, inf),
		\pattDepth, Pn(pattDepth, inf),
	)
};

~filterBeat = { |patt, key, beat, mod, reject|
	var thiskey = key.key;

	Pfunc({ |event|
		var thisEvent, matching;
		//modulo input pattern if set
		if(mod.isNil){
			thisEvent = event[thiskey];
		} {
			thisEvent = event[thiskey].wrap(1, mod);
		};
		//set key to rest if not matching
		// if(beat.includes(thisEvent)){ event }{ event[\instrument] = \rest }}

		matching = beat.includes(thisEvent);
		if(reject == 1){
			matching = matching.not;
		};

		if(matching){ event }{ event[\dur] = Rest(event[\dur]) }}
	);
};

~makeDeltas = { |patt|
		var eventcount = 0;
		var groupcount = 0;
		var groupcountLCM = 0;
		var cyclecount = 0;
		Pbind(
			//delta time in relation to tempo clock
			\bardelta, (Ptime() % t.beatsPerBar) / t.beatsPerBar,
			//janky ass subroutine
			//group delta time
			\groupdelta, Pfunc({|event|
				var groupdelta, cycledelta;
				//event index in group
				event[\eventcount] = eventcount + 1;
				eventcount = eventcount + 1;
				//as delta within group
				groupdelta = (eventcount - 1) / event[\subdiv];

				//index of group
				if(groupcount == event[\groupcycle]){
					groupcount = 0;
				};
				event[\groupcount] = groupcount + 1;

				//index of groupLCM
				if(groupcountLCM == event[\groupcycleLCM]){
					groupcountLCM = 0;
				};
				event[\groupcountLCM] = groupcountLCM + 1;

				if(eventcount == event[\subdiv]){
					eventcount = 0;
					groupcount = groupcount + 1;
					groupcountLCM = groupcountLCM + 1;
				};

				//add cycle count
				event[\cyclecount] = cyclecount + 1;
				cyclecount = cyclecount + 1;
				event[\cycledelta] = (cyclecount - 1) / event[\numevents];

				if(cyclecount == event[\numevents]){
					cyclecount = 0;
				};
				//final return
				groupdelta;
			}),
		)
};

~nearestDelta = { |patt, key, downbeat, centre|
	//wtf does this do tbh
	abs(round(centre - key, downbeat) - (centre - downbeat));
};

SynthDef(\modenvmono, { arg out=0, amp=0.1, gate=1;
	var sig;
	sig = EnvGen.kr(\env.kr(Env([1,1],[0.1])), \itrig.tr(1), doneAction:0);
	sig = sig * EnvGen.kr(Env([1,1,1],[0.1,0.8], releaseNode:1), gate, doneAction:2);
	Out.kr(out, sig);
}).add;

~pmodenv = { arg valpat, timepat=1, repeat=1, curvepat;
	// return a pattern that append a function to \finish which start a Pmono of modenvmono synth outputing an env curve according to patterns from function parameters
	repeat = repeat.clip(1,inf); // prevent infinite loop when puting negative curve in repeat
	Prout({ arg ev;
		var bus = Bus.control(s, 1);
		var timestr;
		var running = true;
		var cleanup = EventStreamCleanup.new;
		var patplayer;
		var finish_fun;
		var cleanup_fun = {
			patplayer.stop;
			running = false;
			//"pmodenv: CLEANUP".debug;
			{
				2.wait;
				if(bus.index.notNil) {
					bus.free;
				}
			}.fork;
		};

		timepat = timepat ??  { Plazy({ ev[\dur] }).loop };
        timepat = Pchain(timepat, Pfunc({ ev }));
		curvepat = curvepat ??  { 0 };
        curvepat = Pchain(curvepat, Pfunc({ ev }));
        valpat = Pchain(valpat, Pfunc({ ev }));

		cleanup.addFunction(ev, cleanup_fun);

		CmdPeriod.doOnce({
			if(bus.index.notNil) {
				bus.free
			}
		});

		finish_fun = {
			patplayer = Pmono(\modenvmono,
				\out, bus,
				\itrig, 1,
				[ \dur, \env ], Prout({ arg monoev;
					var valstr = valpat.asStream;
					var curvestr = curvepat.asStream;
					var previous = valstr.next;
					var time;
					timestr = timepat.asStream;

					block { arg break;
						valstr.do { arg val;
							var prev = previous;
							var curve;
							//val.debug("pmodenv val");
							time = timestr.next;
							curve = curvestr.next;
							if(time.isNil) {
								time = 2;
								monoev[\dur] = time;
								break.value;
							};
							if(curve.isNil) {
								time = 2;
								monoev[\dur] = time;
								break.value;
							};

							//monoev[\dur] = time;
							//Env([prev,val],[time]/thisThread.clock.tempo).asCompileString.debug("env");
							monoev = [time, [ Env([prev,val],[time]/t.tempo, curve) ]].yield;
							// monoev = [time, [ Env([prev,val],[time]/thisThread.clock.tempo, curve) ]].yield;

							previous = val;
						};
					};
					running = false;
					monoev;
				}),
				\legato, 1,
			).play;
		};

		if(ev[\finish].isKindOf(Function)) {
			var oldfun = ev[\finish];
			ev[\finish] = { finish_fun.value; oldfun.value };
		} {
			ev[\finish] = finish_fun;
		};

		while{ running == true } {
			//"pmodenv running".debug;
			var ret = bus.asMap;
			cleanup.update(ev);
			ev = ret.yield;
			// ev.debug("ev");
			if(ev.isNil) {
				"pmodenv: pattern ends".debug; // this never happen
				cleanup_fun.value;
			}
		};
		ev;
	}).repeat(repeat)
};

//skew functions
~arrayDelta = {|array|
	var cumSum = List.new;
	var sum = 0.0;
	var arrSize;

	cumSum.add(0);

	array.do { |item, i|
		sum = sum + item;
		cumSum.add(sum);
	};

	cumSum = cumSum.asArray; 
	cumSum.removeAt(cumSum.size-1);
	cumSum
};

~lcmIndices = {|idx, num, denom|
	var lcm = num*denom;
	// var lcm = lcm(num,denom);
	var lcmSeq = (0..(lcm-1));
	var indices = (0..(denom-1)) * num;
	lcmSeq.at((indices + idx).wrap(0, lcmSeq.size));
};

~eventSkew = {|patt, group=1, skew=0, min=0.1, curve=\lin, numEvents, eventArr|
	var durList, durSize, durSum;
	var env, skewArr, newDur, newDelta;
	
	durList = List.new;

	//get durations as array for group
	numEvents.do({|i|
		var thisKey = eventArr[i].at('groupcountLCM');
		var thisDur = eventArr[i].at('dur').value;
		if(thisKey == group){
			durList.add(thisDur);
		};
	});

	durList = durList.asArray;
	durSize = durList.size;
	
	//skew range -1..1
	skewArr = if(skew >= 0){[skew, min]}{[min, abs(skew)]};

	env = Env(skewArr, [durSize], curve);
	//(durations[n] * sampled envelope[n]) normalised between 1 / scaled to duration of group
	newDur = (durList * env.at((0..durSize-1))).normalizeSum * durList.sum;
	newDelta = ~arrayDelta.(newDur);
	//replace events at group with skewed rhythm
	Pfunc({|ev|
		if(ev[\groupcountLCM] == group) 
		{ 	
			var thisDur = newDur[ev[\eventcount] - 1];
			ev[\dur] = thisDur;
			ev[\groupdelta] = newDelta[ev[\eventcount] - 1];				
		} 
		{ev = ev}
	});
};

~cycleSkew = {|patt, skew=0, min=0.1, curve=\lin, numEvents, eventArr|
	var durList, durSize, durSum;
	var env, skewArr, newDur;
	
	durList = List.new;

	//get durations as array for group
	numEvents.do({|i|
		var thisDur = eventArr[i].at('dur');
		durList.add(thisDur);
	});

	durList = durList.asArray;
	durSize = durList.size;
	
	//skew range -1..1
	skewArr = if(skew >= 0){[skew, min]}{[min, abs(skew)]};

	env = Env(skewArr, [durSize], curve);
	//(durations[n] * sampled envelope[n]) normalised between 1 / scaled to duration of group
	newDur = (durList * env.at((0..durSize-1))).normalizeSum * durList.sum;
	
	//replace events at group with skewed rhythm
	Pfunc({|ev|
		ev[\dur] = newDur[ev[\cyclecount] - 1];
	});
};

~pSkew = {|patt, key, group, skew=0, min=0.1, curve=\lin|
	var chain, idxList, chainList;
	var eventParams, numEvents, pattSize, subdivSize, eventArr, groupLCM;
	var arrSkew, arrCurve;
	var thisKey = key.key;
	
	//get all params
	eventParams = Pn(patt.value, inf).asStream.next(()).asDict;

	case{thisKey=='eventcount'}
	{
		pattSize = eventParams.at('groupcycle');
		subdivSize = eventParams.at('subdivSize');
		numEvents = eventParams.at('numeventsLCM');
		eventArr = patt.value.asStream.nextN(numEvents,());
		group = group.asArray.wrap(1, pattSize).asSet.asArray;
		//wrap group idx to pattern size and remove duplicates
		if(pattSize != subdivSize){

			//get idx multiples of group idx
			idxList = List.new;
			group.do{|elem|
				idxList.add(~lcmIndices.(elem - 1, pattSize, subdivSize) + 1);
			};
			group = idxList.asArray.flat.sort;
		};
		
		if(group.size > 1){
			chainList = List.new;
			// skew for each group idx in passed array
			group.size.do({|i|
				//if array, wrap array values for other params
				if(skew.isKindOf(Array)){ arrSkew = skew[i.wrap(0, skew.size - 1)]}{arrSkew = skew};
				if(curve.isKindOf(Array)){ arrCurve = curve[i.wrap(0, curve.size - 1)]}{arrCurve = curve};
				//add to list
				chainList.add(~eventSkew.(patt, group[i], arrSkew, min, arrCurve, numEvents, eventArr));
			});
			//expand list, applying function to each group
			chain = Pchain(*chainList);
		} {
			chain = ~eventSkew.(patt, group[0], skew, min, curve, numEvents, eventArr);
		}
	}

	{thisKey=='cyclecount'}
	{
		//get array of events from pattern
		numEvents = eventParams.asDict.at('numevents');
		eventArr = patt.value.asStream.nextN(numEvents,());

		if(skew.isKindOf(Array)){ arrSkew = skew[0]}{arrSkew = skew};
		if(curve.isKindOf(Array)){ arrCurve = curve[0]}{arrCurve = curve};

		chain = ~cycleSkew.(patt, arrSkew, min, arrCurve, numEvents, eventArr);
	}
};

~pChainSwitch = {|patt, switch, mod|
	var patternList = List.new;
	if(mod.isNil.not){
		switch = switch.wrap(1, mod)
	};

	patt.do({|elem, i|
		patternList.add(
			Pbind(
				\switch, switch,
				\switchIdx, i + 1,
				\dur, Pfunc({|ev|
					var val;
					// if(ev[\switchIdx] == ev[\switch]){ val = ev[\instrument]}{ val = \rest };
					if(ev[\switchIdx] == ev[\switch]){ val = ev[\dur]}{ val = Rest(ev[\dur]) };
					val
				})
			)
			<> elem
		);
	});
	Ppar(patternList, inf);
};

//wip + doesnt work
~marker = {|sectionLabel, sectionLength, selector|
	var length;
	sectionLabel.postln;
	length = case
	{ selector == 'all'} { sectionLength }
	{ selector == sectionLabel} { sectionLength }
	{ selector != sectionLabel} { 0 };
	length;
};
)
