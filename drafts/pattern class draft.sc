///COMPOSTION FUNCTIONS
(
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
)

(
	~makeSubdivisionPhasing = {|patt, subdiv, bounds=\max|
		var pattStream = patt.asStream;
		var subdivStream = subdiv.asStream;

		var pattSize = patt.list.size;
		var subdivSize = subdiv.list.size;

		// var pattSize = ~subPatternSize.(patt);
		// var subdivSize = ~subPatternSize.(subdiv);
		// var pattDepth = ~subPatternDepth.(patt);
		
		var pattBounds =
		case
			{bounds==\min}{ min(pattSize, subdivSize); }
			{bounds==\max}{ max(pattSize, subdivSize); }
			{bounds==\lcm}{ lcm(pattSize, subdivSize);};

		Plazy({
			var patVals = Array.fill(pattBounds, { pattStream.next });
			var subdivVals = Array.fill(pattBounds, { subdivStream.next });
			var safeSubdivs = subdivVals.collect({ |x| if(x == 0){ 1 }{ x } });
			var numEvents = safeSubdivs.sum;

			var cyclecount = (1..numEvents);
			var cycledelta = cyclecount.collect({ |c| (c-1)/numEvents });

			var eventcount = safeSubdivs.collect({ |s| (1..s) }).flatten;
			var groupcount = safeSubdivs.collect({ |s, i| (i+1).dup(s) }).flatten;
			var groupdelta = eventcount.collect({ |e, i| (e-1)/safeSubdivs.wrapAt(groupcount[i]-1) });

			var subdivs = safeSubdivs.collect({|i| i.dup(i)}).flatten;

			var durArray = patVals.collect({ |patVal, i|
				var safeSubdiv = safeSubdivs[i];
				var subdivVal = subdivVals[i];

				Array.fill(safeSubdiv, {
					var d = patVal / safeSubdiv;
					if(subdivVal == 0){ d = Rest(d) };
					d
				})

			}).flatten;

			Pbind(
				\dur,        Pseq(durArray, 1),
				\eventcount, Pseq(eventcount, 1),
				\groupcount, Pseq(groupcount, 1),
				\cyclecount, Pseq(cyclecount, 1),
				\groupdelta, Pseq(groupdelta, 1),
				\cycledelta, Pseq(cycledelta, 1),
				\numEvents,  Pseq(numEvents.dup(numEvents), 1),
				\subdivs,  Pseq(subdivs, 1),
				// \groupSizes, Pseq(safeSubdivs.dup(numEvents), 1)
			)
		}).repeat
	};

	~pApply = { |patt, func|
		var sampleStream = patt.asStream;
		var transformation = { |ev| ev };
		Pfunc({ |ev|
			if(ev[\cyclecount] == 1) {
				var eventArr = sampleStream.nextN(ev[\numEvents], ());
				transformation = func.(eventArr);
			};
			transformation.(ev)
		})
	};

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

	~makeSkewEnv = { |durs, skew, min, curve|
		var skewArr = if(skew >= 0){ [skew, min] }{ [min, abs(skew)] };
		var env = Env(skewArr, [durs.size], curve);
		(durs * env.at((0..durs.size-1))).normalizeSum * durs.sum
	};

	~eventSkew = { |eventArr, group=1, skew=0, curve=\lin, min=0.01|
		if(skew == 0) { { |ev| ev } } {
			var durArray = eventArr.collect(_[\dur]);
			var groupcount = eventArr.collect(_[\groupcount]);
			var groupDurs = durArray.select({ |d, i| group.asArray.includes(groupcount[i]) });
			var newDur = ~makeSkewEnv.(groupDurs, skew, min, curve);
			var newDelta = ~arrayDelta.(newDur) / newDur.sum;
			var groupEventIndex = 0;
			var newDurFull = durArray.collect({ |d, i|
				if(group.asArray.includes(groupcount[i])) {
					var val = newDur[groupEventIndex];
					groupEventIndex = groupEventIndex + 1;
					val
				}{ d }
			});
			var newCycleDelta = ~arrayDelta.(newDurFull) / newDurFull.sum;

			{ |ev|
				if(group.asArray.includes(ev[\groupcount])) {
					ev[\dur] = newDur[ev[\eventcount] - 1];
					ev[\groupdelta] = newDelta[ev[\eventcount] - 1];
				};
				ev[\cycledelta] = newCycleDelta[ev[\cyclecount] - 1];
				ev
			}
		}
	};

	~cycleSkew = { |eventArr, skew=0, curve=\lin, min=0.01|
		if(skew == 0) { { |ev| ev } } {
			var durArray = eventArr.collect(_[\dur]);
			var groupSizes = eventArr.collect(_[\subdivs]).separate({ |a, b| a != b }).collect(_.first);
			var newDur = ~makeSkewEnv.(durArray, skew, min, curve);
			var newCycleDelta = ~arrayDelta.(newDur) / newDur.sum;

			var newGroupDelta = newDur.clumps(groupSizes)
				.collect({ |grp| ~arrayDelta.(grp) / grp.sum })
				.flatten;

			{ |ev|
				ev[\dur] = newDur[ev[\cyclecount] - 1];
				ev[\cycledelta] = newCycleDelta[ev[\cyclecount] - 1];
				ev[\groupdelta] = newGroupDelta[ev[\cyclecount] - 1];
				ev
			}
		}
	};

	~pSkew = { |patt, key=\eventcount, group=1, skew=0, curve=\lin, min=0.01|
		var groupStream = group.asStream;
		var skewStream = skew.asStream;
		var curveStream = curve.asStream;

		~pApply.(patt, { |cycleData|
			switch(key,
				\eventcount, { ~eventSkew.(cycleData, groupStream.next.asArray, skewStream.next, curveStream.next, min)},
				\cyclecount, { ~cycleSkew.(cycleData, skewStream.next, curveStream.next, min) }
			)
		})
	};

	~pFilter = { |key, index, mod, reject=0|
		var thiskey = key;
		var idxStream = index.asStream;
		var modStream = mod.asStream;
		var rejectStream = reject.asStream;
		var thisIdx, thisMod, thisReject;

		Pfunc({ |ev|
			var thisEvent, matching;
			if(ev[key] == 1) {
				thisIdx = idxStream.next.asArray;
				thisMod = modStream.next;
				thisReject = rejectStream.next;
			};

			thisEvent = if(thisMod.isNil){
				ev[thiskey]
			}{
				ev[thiskey].wrap(1, thisMod)
			};

			matching = thisIdx.includes(thisEvent);
			if(thisReject.asBoolean){ matching = matching.not };
			if(matching){ ev }{ ev[\dur] = Rest(ev[\dur]) }
		})
	};

	~pAccent = { |key, valueKey, value, index, mod, reject=0|
		var thiskey = key;
		var valueStream = value.asStream;
		var idxStream = index.asStream;
		var modStream = mod.asStream;
		var rejectStream = reject.asStream;
		var thisIdx, thisMod, thisReject, thisValue;

		Pfunc({ |ev|
			var thisEvent, matching;
			if(ev[key] == 1) {
				thisIdx = idxStream.next.asArray;
				thisMod = modStream.next;
				thisReject = rejectStream.next;
				thisValue = valueStream.next;
			};

			thisEvent = if(thisMod.isNil){
				ev[thiskey]
			}{
				ev[thiskey].wrap(1, thisMod)
			};
			
			matching = thisIdx.includes(thisEvent);
			if(thisReject.asBoolean){ matching = matching.not };
			if(matching){ ev[valueKey] = ev[valueKey] * thisValue };
			ev;
		})
	};

	~pRotate = { |patt, key=\cyclecount, group=nil, n=1|
		var nStream = n.asStream;
		var groupStream = group.asStream;

		~pApply.(patt, { |eventArr|
			var durArray = eventArr.collect(_[\dur]);
			var groupcount = eventArr.collect(_[\groupcount]);
			var groupSizes = eventArr.collect(_[\subdivs]).separate({ |a, b| a != b }).collect(_.first);
			var thisN = nStream.next;
			var thisGroup = groupStream.next;

			var newDurArray = switch(key,
				\cyclecount, {
					durArray.rotate(thisN)
				},
				\eventcount, {
					var groupEventIndex = 0;
					var result = durArray.copy;
					groupSizes.do({ |s, i|
						if(thisGroup.isNil || { thisGroup.asArray.includes(i+1) }) {
							var slice = result.copyRange(groupEventIndex, groupEventIndex + s - 1);
							slice.rotate(thisN).doWithIndex({ |d, j|
								result[groupEventIndex + j] = d;
							});
						};
						groupEventIndex = groupEventIndex + s;
					});
					result
				}
			);

			var newCycleDelta = ~arrayDelta.(newDurArray) / newDurArray.sum;
			var newGroupDelta = newDurArray.clumps(groupSizes)
				.collect({ |grp| ~arrayDelta.(grp) / grp.sum })
				.flatten;

			{ |ev|
				ev[\dur] = newDurArray[ev[\cyclecount] - 1];
				ev[\cycledelta] = newCycleDelta[ev[\cyclecount] - 1];
				ev[\groupdelta] = newGroupDelta[ev[\cyclecount] - 1];
				ev
			}
		})
	};

	//more ideas
	//pRotate
	//pRepeat / subdivide
	//pMask
	//pAppend -- append two cycles and recalc data
	//pDrop drop event/group and recalculate data
	//pRepeat repeat range and recalculate data?
	//skew env version
)



//usage
(
	var patt = PlaceAll([1, 2, 1, 1] * 2, inf);
	var subdiv = Pseq([4, 4, 4,], inf);
	
	Pdef(\p1, ~makeSubdivisionPhasing.(patt, subdiv, bounds: \max));

	(
		~pAccent.(
			key: \eventcount, 
			valueKey: \amp, 
			value: Pseq([0.1, 0.5], inf),
			index: Pseq([1, 3, 4], inf), 
			mod: 5,
			reject: 1
		) <>

		~pFilter.(
			key: \eventcount, 
			index: Pseq([1, 3], inf), 
			// mod: 5, 
			reject: 1,
			// reject: Pseq([1, 0, 1], inf),
		) <>

		~pSkew.(Pdef(\p1), key: \cyclecount, group: [1, 3], skew: Pwhite(1, -1, inf), curve: \exp) <>
		Pbind(\amp, 1, \instrument, \pulsePluck, \degree, Pkey(\eventcount) + Pkey(\groupcount), \octave, 1) <>
		Pdef(\p1)
	// ).play(t);
	).play(t);
)


(
Event.addEventType(\counted, {
    if(Event.eventTypes[\countedState].isNil) {
        Event.eventTypes[\countedState] = 0
    };
    Event.eventTypes[\countedState] = Event.eventTypes[\countedState] + 1;

    // Make it available as an event key
    ~count = Event.eventTypes[\countedState];

    ("Event #" ++ ~count ++ " | freq: " ++ ~freq).postln;
});
)

(
Pbind(
    \type, \counted,
    \freq, Pseq([440, 660, 880], inf),
    \dur, 0.5
).trace.play;
)

(
Pbind(
    \type, \counted,
    \freq, Pseq([440, 660, 880], inf),
    \dur, 0.5
).trace.play;
)

(
Event.addEventType(\counted, {
    var key = ("count_" ++ ~streamID).asSymbol;
    Event.eventTypes[key] = (Event.eventTypes[key] ?? { 0 }) + 1;
    currentEnvironment[\count] = Event.eventTypes[key];  // explicit write
    // ("Stream: " ++ ~streamID ++ " | Event #" ++ ~count ++ " | freq: " ++ ~freq).postln;
});
)

(
Pbind(
    \type, \counted,
    \streamID, \a,
    \freq, Pseq([440, 550, 660], inf),
    \dur, 0.5
).trace(\count).play;
)
// Pbind(
//     \type, \counted,
//     \streamID, \b,
//     \freq, Pseq([880, 770, 660], inf),
//     \dur, 0.75
// // ).play;
// )

Pscratch

cumulative