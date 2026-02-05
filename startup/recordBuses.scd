(
    // IN:
    // 'buses' should be an array of bus objects
    // 'path' should be "/path/to/recordings/name%.wav"
    // and the '%' will be filled in with the bus number
    
    // OUT:
    // array of Recorder objects -- use these to stop recording
    
    ~recordBuses = { |buses, path, recHeaderFormat = "wav", recSampleFormat = "float", recBufSize = 131072, latency = 0.2|
        var dir = path.dirname;
        var recorders, servers = Set.new;
        var digits = 1;
        
        var pad = { |num, digits|
            num = num.asString;
            if(num.size < digits) {
                String.fill(digits - num.size, $0) ++ num
            } {
                num
            }
        };
        
        if(File.exists(dir)) {
            if(File.type(dir) != \directory) {
                Error(
                    "Path '%' exists but is not a directory"
                    .format(dir)
                ).throw;
            }
        } {
            // didn't exist
            // throws error upon failure (good)
            File.mkdir(dir);
        };
        
        recorders = buses.collect { |bus|
            digits = max(digits, bus.server.options.numAudioBusChannels.log10.ceil.asInteger);
            servers.add(bus.server);
            Recorder(bus.server)
            .recHeaderFormat_(recHeaderFormat)
            .recSampleFormat_(recSampleFormat)
            .numChannels_(bus.numChannels)
            .recBufSize_(recBufSize);
        };
        
        fork {
            recorders.do { |rec, i|
                rec.prepareForRecord(
                    // path.format(pad.value(buses[i].index, digits)),
                    path.format(pad.value(buses[i].envirKey ++ "_" ++ buses[i].index, digits)),
                    buses[i].numChannels  // yes, must pass again, hm
                );
            };
            
            // normally this will be just one server,
            // but let's handle the unusual case too
            servers.do { |server| server.sync };
            
            servers.do { |server|
                server.makeBundle(latency, {
                    recorders.do { |rec, i|
                        if(rec.server == server) {
                            rec.record(bus: buses[i]);
                        };
                    };
                });
            };
        };
        
        recorders
    };
    
    ~stopRecording = { |recorders|
        recorders.do { |rec| rec.stopRecording };
    };
)