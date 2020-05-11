/*
 * Copyright (C) 2012-2020, Markus Sprunck <sprunck.markus@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.sw_engineering_candies.yaca;

import java.util.logging.Level;

import lombok.extern.java.Log;

import org.springframework.web.bind.annotation.*;

@RestController
@Log
public class RestService {

    private final Model model;

    private String options = "";

    RestService(Model model, AsynchronousAnalyserService asynchronousAnalyserService) {
        this.model = model;
        asynchronousAnalyserService.executeAsynchronously();
    }

    @GetMapping("/analyzer/options")
    String getOptions() {
        return this.options;
    }

    @PutMapping("/analyzer/options")
    void putOptions(String options) {
        this.options = options;
    }

    @GetMapping("/process/")
    String getProcess() {
        return model.getJSONPModel();
    }

    @GetMapping("/process/ids")
    String getProcessIDs() throws InterruptedException {
        CallStackAnalyzer.findOtherAttachableJavaVMs();
        log.info("VirtualMachines=" + CallStackAnalyzer.allVirtualMachines);
        Thread.sleep(10);
        return model.getJSONPVM();
    }

    @PutMapping("/process/id")
    void putProcessId(@RequestBody String id) {
        CallStackAnalyzer.setProcessNewID(id);
    }

    @DeleteMapping("/tasks")
    void deleteTasks() {
        log.log(Level.INFO, "delete tasks called");
    }

    @DeleteMapping("/analyzer")
    void deleteAnalyser() {
        log.info("Server stopped");
        System.exit(0);
    }

    @PutMapping("/filterBlack")
    void putFilterBlack(@RequestParam String filterBlack) {
        model.setFilterBlackList(filterBlack);
    }

    @GetMapping("/filterBlack")
    String putFilterBlack() {
        return model.getFilterBlackList();
    }

    @DeleteMapping("/filterBlack")
    void deltFilterBlack() {
        model.setFilterBlackList("");
    }


    @PutMapping("/filterWhite")
    void putFilterWhite(@RequestParam String body) {
        model.setFilterWhiteList(body);
    }

    @GetMapping("/filterWhite")
    String putFilterWhite() {
        return model.getFilterWhiteList();
    }

    @DeleteMapping("/filterWhite")
    void delFilterWhite() {
        model.setFilterWhiteList("");
    }

}