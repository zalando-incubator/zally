package commands

import (
	"fmt"
	"io/ioutil"
	"net/http"

	"encoding/json"

	"bytes"

	"path/filepath"

	"strings"

	"github.com/urfave/cli"
	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/utils"
	"github.com/zalando/zally/cli/zally/utils/formatters"
)

// LintCommand lints given API definition file
var LintCommand = cli.Command{
	Name:      "lint",
	Usage:     "Lint given `FILE` with API definition",
	Action:    lint,
	ArgsUsage: "FILE",
}

func lint(c *cli.Context) error {
	if !c.Args().Present() {
		cli.ShowCommandHelp(c, c.Command.Name)
		return fmt.Errorf("Please specify Swagger File")
	}

	formatter, err := formatters.NewFormatter(c.GlobalString("format"))
	if err != nil {
		cli.ShowCommandHelp(c, c.Command.Name)
		return err
	}

	path := c.Args().First()
	requestBuilder := utils.NewRequestBuilder(c.GlobalString("linter-service"), c.GlobalString("token"), c.App)

	return lintFile(path, requestBuilder, formatter)
}

func lintFile(path string, requestBuilder *utils.RequestBuilder, formatter formatters.Formatter) error {
	data, err := readFile(path)
	if err != nil {
		return err
	}

	violations, err := doRequest(requestBuilder, data)
	if err != nil {
		return err
	}

	numberOfMustViolations := len(violations.Must())
	if numberOfMustViolations > 0 {
		err = fmt.Errorf("Failing because: %d must violation(s) found", numberOfMustViolations)
	}

	var buffer bytes.Buffer
	resultPrinter := utils.NewResultPrinter(&buffer, formatter)
	resultPrinter.PrintViolations(violations)

	fmt.Print(buffer.String())

	return err
}

func readFile(path string) (string, error) {
	var contents []byte
	var err error

	if strings.HasPrefix(path, "http://") || strings.HasPrefix(path, "https://") {
		contents, err = readRemoteFile(path)
	} else {
		contents, err = readLocalFile(path)
	}

	if err != nil {
		return "", err
	}

	return string(contents), nil
}

func readLocalFile(path string) ([]byte, error) {
	absolutePath, err := filepath.Abs(path)
	if err != nil {
		return nil, err
	}

	return ioutil.ReadFile(absolutePath)
}

func readRemoteFile(url string) ([]byte, error) {
	response, err := http.Get(url)
	if err != nil {
		return nil, err
	}

	defer response.Body.Close()
	return ioutil.ReadAll(response.Body)
}

func doRequest(requestBuilder *utils.RequestBuilder, data string) (*domain.Violations, error) {
	var apiViolationsRequest domain.APIViolationsRequest
	apiViolationsRequest.APIDefinitionString = data
	requestBody, err := json.MarshalIndent(apiViolationsRequest, "", "  ")
	if err != nil {
		return nil, err
	}

	request, err := requestBuilder.Build("POST", "/api-violations", bytes.NewBuffer(requestBody))
	if err != nil {
		return nil, err
	}

	response, err := utils.DoHTTPRequest(request)
	if err != nil {
		return nil, err
	}

	if response.StatusCode != 200 {
		defer response.Body.Close()
		body, _ := ioutil.ReadAll(response.Body)

		return nil, fmt.Errorf(
			"Cannot submit file for linting. HTTP Status: %d, Response: %s", response.StatusCode, string(body))
	}

	decoder := json.NewDecoder(response.Body)
	var violations domain.Violations
	err = decoder.Decode(&violations)
	if err != nil {
		return nil, err
	}

	return &violations, nil
}
